package kpchuck.k_klock

import android.content.Context
import android.content.pm.PackageManager

/**
 * Created by karol on 18/02/18.
 * Copied from http://github.com/substratum/template
 */
object Checks {

    fun getSelfVerifiedPirateTools(context: Context): Boolean {
        BLACKLISTED_APPLICATIONS
                .filter { isPackageInstalled(context, it) }
                .forEach { return true }
        return false
    }


    fun isPackageInstalled(context: Context, package_name: String): Boolean {
        return try {
            val pm = context.packageManager
            val ai = context.packageManager.getApplicationInfo(package_name, 0)
            pm.getPackageInfo(package_name, PackageManager.GET_ACTIVITIES)
            ai.enabled
        } catch (e: Exception) {
            false
        }
    }


    // Blacklisted APKs to prevent theme launching, these include simple regex formatting, without
    // full regex formatting (e.g. com.android. will block everything that starts with com.android.)
    val BLACKLISTED_APPLICATIONS = arrayOf(
            "cc.madkite.freedom",
            "zone.jasi2169.uretpatcher",
            "uret.jasi2169.patcher",
            "p.jasi2169.al3",
            "com.dimonvideo.luckypatcher",
            "com.chelpus.lackypatch",
            "com.forpda.lp",
            "com.android.vending.billing.InAppBillingService.LUCK",
            "com.android.vending.billing.InAppBillingService.CLON",
            "com.android.vending.billing.InAppBillingService.LOCK",
            "com.android.vending.billing.InAppBillingService.CRAC",
            "com.android.vending.billing.InAppBillingService.LACK",
            "com.android.vendinc",
            "com.appcake",
            "ac.market.store",
            "org.sbtools.gamehack",
            "com.zune.gamekiller",
            "com.aag.killer",
            "com.killerapp.gamekiller",
            "cn.lm.sq",
            "net.schwarzis.game_cih",
            "org.creeplays.hack",
            "com.baseappfull.fwd",
            "com.zmapp",
            "com.dv.marketmod.installer",
            "org.mobilism.android",
            "com.blackmartalpha",
            "org.blackmart.market"
    )

}