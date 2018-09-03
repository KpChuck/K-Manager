package kpchuck.kklock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.github.javiersantos.piracychecker.PiracyChecker
import com.github.javiersantos.piracychecker.enums.InstallerID
import com.github.javiersantos.piracychecker.enums.PiracyCheckerCallback
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError
import com.github.javiersantos.piracychecker.enums.PirateApp
import kpchuck.kklock.utils.FileHelper
import kpchuck.kklock.utils.PrefUtils

/**
 * Created by karol on 18/02/18.
 * Copied from http://github.com/substratum/template
 */
class Checks {

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

    fun checkPro(context: Context){
        Log.d("klock", "Starting pro check")
        PrefUtils(context).putBool("hellothere", false)
        val manager = context.packageManager
        if (manager.checkSignatures("kpchuck.k_klock", "kpchuck.k_klock.pro") == PackageManager.SIGNATURE_MATCH) {

            val i = Intent()
            i.action = "kpchuck.k_klock.pro.send"
            i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            i.component = ComponentName("kpchuck.k_klock.pro", "kpchuck.k_klock.pro.CheckProReceiver")
            Log.d("klock", "Sending broadcast to pro app")
            context.sendBroadcast(i)
        }
    }

    fun isPro(context: Context): Boolean{

        if (context.packageManager.checkSignatures("kpchuck.k_klock", "kpchuck.k_klock.pro") == PackageManager.SIGNATURE_MATCH) {
            return PrefUtils(context).getBool("hellothere")
        }
        return false
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