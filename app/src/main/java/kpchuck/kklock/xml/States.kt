package kpchuck.kklock.xml

import android.util.Xml
import kpchuck.kklock.R
import kpchuck.kklock.utils.PrefUtils

class States(val prefUtils: PrefUtils) {

    fun movingStatusbarIcons() = prefUtils.getInt(R.string.key_move_network) != XmlUtils.RIGHT

    fun hasCustomOrCenterClock() = (!isStockClock() || isCenterClock())

    fun shouldModifyClock() = getClockPosition() == XmlUtils.NONE || getClockPosition() == XmlUtils.USE_SYSTEM


    // region getting pref utils

    fun isOos() = prefUtils.getBool(R.string.key_oos_is_bad)

    fun isStockClock() = prefUtils.getBool(R.string.key_stock_style);

    fun isCustomClock() = !isStockClock()

    fun getClockPosition() = prefUtils.getInt(R.string.key_clock_position)

    fun isClockOnLockscreen() = prefUtils.getBool(R.string.key_sb_clock_on_lockscreen);

    // endregion

    // region clock positions

    fun isCenterClock() = getClockPosition() == XmlUtils.CENTER

    fun isRightClock() = getClockPosition() == XmlUtils.RIGHT

    fun isLeftClock() = getClockPosition() == XmlUtils.LEFT

    fun isCenterOrLeftClock() = getClockPosition() == XmlUtils.CENTER || getClockPosition() == XmlUtils.LEFT

    // endregion
}