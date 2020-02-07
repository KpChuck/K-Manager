package brut.androlib.res.decoder

import android.content.pm.PackageManager
import android.content.res.Resources
import androidapktool.util.TypedValue
import brut.androlib.AndrolibException
import brut.androlib.res.xml.ResXmlEncoders
import java.util.logging.Level
import java.util.logging.Logger

class AXmlCtxResourceParser(private val pm: PackageManager, private val onlyAndroid: Boolean): AXmlResourceParser() {


    private fun getPackageRes(): Resources? {
        if (onlyAndroid) return null
        return try {
            val currentPkg = attrDecoder.currentPackage
            val pkgName = currentPkg.name
            pm.getResourcesForApplication(pkgName)
        } catch (e: PackageManager.NameNotFoundException){
            null
        }
    }

    override fun getAttributeValue(index: Int): String {
        val offset = getAttributeOffset(index)
        val valueType = m_attributes[offset + ATTRIBUTE_IX_VALUE_TYPE]
        val valueData = m_attributes[offset + ATTRIBUTE_IX_VALUE_DATA]
        val valueRaw = m_attributes[offset + ATTRIBUTE_IX_VALUE_STRING]

        if (attrDecoder != null) {
            try {
                var decoded = attrDecoder.decode(
                    valueType,
                    valueData,
                    if (valueRaw == -1) null else ResXmlEncoders.escapeXmlChars(
                        m_strings.getString(
                            valueRaw
                        )
                    ),
                    getAttributeNameResource(index)
                )

                if (decoded == "@null") {
                    decoded = getEntryNameWithRes(valueData)
                }
                if (decoded == "@null"){
                    LOGGER.log(
                            Level.WARNING, String.format("Could not decode attr value, using undecoded value "
                                    + "instead: ns=%s, name=%s, value=0x%08x", getAttributePrefix(index), getAttributeName(index), valueData)
                    )
                }
                return decoded
            } catch (ex: AndrolibException) {
                firstError = ex
                LOGGER.log(
                    Level.WARNING, String.format(
                        "Could not decode attr value, using undecoded value "
                                + "instead: ns=%s, name=%s, value=0x%08x",
                        getAttributePrefix(index),
                        getAttributeName(index),
                        valueData
                    ), ex
                )
            }
        }
        return TypedValue.coerceToString(valueType, valueData)
    }

    private fun getEntryNameWithRes(id: Int): String{
        // Get pkg manager
        val res = getPackageRes()
        return try {
            val value = res?.getResourceEntryName(id)
            var pkg = res?.getResourcePackageName(id)!!
            pkg = if (pkg.contains(attrDecoder.currentPackage.name))
                ""
            else
                "android:"
            val type = res.getResourceTypeName(id)
            "@$pkg$type/$value"
        } catch (e: Resources.NotFoundException){
            "@null"
        }
    }


    companion object {
        private val LOGGER = Logger.getLogger(AXmlCtxResourceParser::class.java.name)
    }


}