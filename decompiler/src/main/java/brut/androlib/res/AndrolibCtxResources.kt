package brut.androlib.res

import android.content.pm.PackageManager
import brut.androlib.AndrolibException
import brut.androlib.ApkOptions
import brut.androlib.res.data.*
import brut.androlib.res.data.value.*
import brut.androlib.res.decoder.*
import brut.androlib.res.xml.ResValuesXmlSerializable
import brut.directory.Directory
import brut.directory.DirectoryException
import brut.directory.ExtFile
import brut.directory.FileDirectory
import brut.util.Duo
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.logging.Logger

class AndrolibCtxResources(options: ApkOptions, private val pm: PackageManager): AndrolibResources() {

    init {
        apkOptions = options
    }

    override fun getResTable(apkFile: ExtFile?, loadMainPkg: Boolean): ResTable {
        val resTable = ResTable(this, pm)
        if (loadMainPkg) {
            loadMainPkg(resTable, apkFile)
        }
        return resTable
    }

    fun getPartialResTable(apkFile: ExtFile?): ResTable {
        val resTable = ResTable(this, pm)
        loadMainPkgPartial(resTable, apkFile)
        return resTable
    }

    fun loadMainPkgPartial(resTable: ResTable?, apkFile: ExtFile?): ResPackage {
        LOGGER.info("Loading resource table...")
        val pkgs = getResPackagesFromApkUntil(apkFile, resTable, sKeepBroken, "attr")
        var pkg: ResPackage? = null

        when (pkgs.size) {
            1 -> pkg = pkgs[0]
            2 -> {
                if (pkgs[0].name == "android") {
                    LOGGER.warning("Skipping \"android\" package group")
                    pkg = pkgs[1]
                } else if (pkgs[0].name == "com.htc") {
                    LOGGER.warning("Skipping \"htc\" package group")
                    pkg = pkgs[1]
                }
                pkg = selectPkgWithMostResSpecs(pkgs)
            }
            else -> pkg = selectPkgWithMostResSpecs(pkgs)
        }

        if (pkg == null) {
            throw AndrolibException("arsc files with zero packages or no arsc file found.")
        }

        resTable!!.addPackage(pkg, true)
        return pkg
    }

    fun decodeLayout(resTable: ResTable, apkFile: ExtFile, outDir: File?, layouts: List<String>) {
        val duo = resFileDecoder
        val fileDecoder = duo.m1
        val attrDecoder = duo.m2.attrDecoder

        attrDecoder.currentPackage = resTable.listMainPackages().iterator().next()
        val inApk: Directory
        var `in`: Directory? = null
        var out: Directory

        try {
            out = FileDirectory(outDir)
            inApk = apkFile.directory
            out = out.createDir("res")
            if (inApk.containsDir("res")) {
                `in` = inApk.getDir("res")
            }
            if (`in` == null && inApk.containsDir("r")) {
                `in` = inApk.getDir("r")
            }
            if (`in` == null && inApk.containsDir("R")) {
                `in` = inApk.getDir("R")
            }
        } catch (ex: DirectoryException) {
            throw AndrolibException(ex)
        }

        val xmlSerializer = resXmlSerializer
        val pkg = resTable.listMainPackages().first()
        for (name in layouts) {
            val strippedName = name.substringAfterLast('/').substringBeforeLast('.')
            val resources = pm.getResourcesForApplication(pkg.name)
            val id = resources.getIdentifier(name, "layout", pkg.name)

            val resFile = ResFileValue("res/$name", id)
            val resType = ResTypeSpec("layout", resTable, pkg, id, 1)
            val spec = ResResSpec(ResID(id), strippedName, pkg, resType)
            val resResource = ResResource(ResType(ResConfigFlags()), spec, resFile)

            LOGGER.info("Decoding file-resources...")
            fileDecoder.decode(resResource, `in`, ExtFile(outDir).directory)
            LOGGER.info("Generated xml file " + name)
        }

        val attrsOutput = FileOutputStream(File(outDir, "attrs.xml"))
        xmlSerializer.setOutput(attrsOutput, null)
        xmlSerializer.startDocument(null, null)
        xmlSerializer.startTag(null, "resources")

        for (p in resTable.listMainPackages()){
            for (spec in p.getType("attr").listResSpecs()) {
                for (res in spec.listResources()) {
                    val defres = spec.defaultResource
                    (defres.value as ResValuesXmlSerializable).serializeToResValuesXml(xmlSerializer, defres)
                }
            }
        }
        xmlSerializer.endTag(null, "resources")
        xmlSerializer.newLine()
        xmlSerializer.flush()
        xmlSerializer.endDocument()
        attrsOutput.close()

        val decodeError = duo.m2.firstError
        if (decodeError != null) {
            throw decodeError
        }
    }

    override fun getResFileDecoder(): Duo<ResFileDecoder, AXmlResourceParser> {
        val decoders = ResStreamDecoderContainer()
        decoders.setDecoder("raw", ResRawStreamDecoder())
        decoders.setDecoder("9patch", Res9patchStreamDecoder())

        val axmlParser = AXmlCtxResourceParser(pm, false)
        axmlParser.attrDecoder = ResAttrDecoder()
        decoders.setDecoder("xml", XmlPullStreamDecoder(axmlParser, resXmlSerializer))

        return Duo(ResFileDecoder(decoders), axmlParser)
    }

    private fun getResPackagesFromApkUntil(apkFile: ExtFile?, resTable: ResTable?, keepBroken: Boolean, until: String
    ): Array<ResPackage> {
        return try {
            val dir = apkFile!!.directory
            val bfi = BufferedInputStream(dir.getFileInput("resources.arsc"))
            try {
                ARSCDecoder.decodeUntil(bfi, false, keepBroken, until, resTable).packages
            } finally {
                try {
                    bfi.close()
                } catch (ignored: IOException) {
                }
            }
        } catch (ex: DirectoryException) {
            throw AndrolibException("Could not load resources.arsc from file: $apkFile", ex)
        }
    }

    override fun loadFrameworkPkg(resTable: ResTable?, id: Int, frameTag: String?): ResPackage {
        val apk = getFrameworkApk(id, frameTag)
        LOGGER.info("Loading resource table partially from file: $apk")

        val mFramework = ExtFile(apk)
        val pkgs = getResPackagesFromApkUntil(mFramework, resTable, true, "attr")

        val pkg: ResPackage
        pkg = if (pkgs.size > 1) {
            selectPkgWithMostResSpecs(pkgs)
        } else if (pkgs.size == 0) {
            throw AndrolibException("Arsc files with zero or multiple packages")
        } else {
            pkgs[0]
        }

        if (pkg.id != id) {
            throw AndrolibException("Expected pkg of id: " + id.toString() + ", got: " + pkg.id)
        }

        resTable!!.addPackage(pkg, false)
        return pkg
    }

    override fun getFrameworkApk(id: Int, frameTag: String?): File {
        val src = pm.getPackageInfo("android", 0).applicationInfo.sourceDir
        return File(src)
    }

    companion object {
        val LOGGER = Logger.getLogger(AndrolibCtxResources::class.java.name)
    }
}