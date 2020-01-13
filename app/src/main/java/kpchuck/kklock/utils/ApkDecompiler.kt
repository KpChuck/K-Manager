package kpchuck.kklock.utils

import android.content.pm.PackageManager
import brut.androlib.ApkOptions
import brut.androlib.res.AndrolibCtxResources
import brut.directory.ExtFile
import org.apache.commons.io.IOUtils
import java.io.File

class ApkDecompiler {

    var input: ExtFile? = null

    fun decompile(pm: PackageManager, inApk: File){
        input = ExtFile(inApk)
        val outApkDir = ExtFile(inApk.parent)
        deleteEverythingExcept(inApk, outApkDir)

        val options = ApkOptions()
        val androlibRes = AndrolibCtxResources(options, pm)
        val resTable = androlibRes.getPartialResTable(input)
        val xmlInputs = ArrayList<String>()
        for (x in xmls)
            xmlInputs.addAll(findFiles(x, true))
        for (x in xmlsUnexact)
            xmlInputs.addAll(findFiles(x, false))
        androlibRes.decodeLayout(resTable, input!!, outApkDir, xmlInputs)
        flattenDirectory(outApkDir)
        androlibRes.close()
    }

    private fun flattenDirectory(file: ExtFile){
        val dir = file.directory
        for (f in dir.getFiles(true)){
            if (f.contains('/')) {
                val name = f.substringAfterLast('/')
                dir.getFileOutput(name).use { o ->
                    dir.getFileInput(f).use { it.copyTo(o) }
                }
            }
        }
        for (dirs in dir.getDirs(false)){
            File(file, dirs.key).deleteRecursively()
        }
    }

    private fun findFiles(search: String, exact: Boolean): List<String> {
        if (input == null) return ArrayList()
        val res = input!!.directory.getDir("res")
        val result = ArrayList<String>()
        for (f in res.getFiles(true)){
            var name = f.substringAfterLast('/')
            name = name.substringBeforeLast('.')
            if (exact && name == search)
                result.add(f)
            else if (!exact && name.contains(search))
                result.add(f)
        }
        return result
    }

    private fun deleteEverythingExcept(inFile: File, targetDir: File){
        for (file in targetDir.list()){
            if (file != inFile.name)
                File(targetDir, file).deleteRecursively()
        }
    }

    companion object {
        private val xmls = listOf("status_bar", "keyguard_status_bar", "system_icons", "status_bar_contents_container")
        private val xmlsUnexact = listOf("quick_status_bar_expanded_header")
    }
}