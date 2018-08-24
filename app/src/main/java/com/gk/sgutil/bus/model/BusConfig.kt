package com.gk.sgutil.bus.model

import android.content.Context
import android.content.SharedPreferences
import com.gk.sgutil.R
import javax.inject.Inject

/**
 * Wraps SharedPreferences with default settings
 */
class BusConfig @Inject constructor(context: Context) {

    companion object {
        // Runtime
        const val LAST_BUS_STOPS_SYNC_TIME = "last_bus_stops_sync_time"
        const val LAST_BUS_ROUTES_SYNC_TIME = "last_bus_routes_sync_time"

        // Configurations which have default value from xml
        const val BUS_DATA_EXPIRY_TIME = "bus_data_expiry_time"
        const val BUS_STOP_SEARCH_RANGE = "bus_stops_search_range"
        const val LOCATION_MIN_MOVE_TO_UPDATE = "location_min_move_to_update"

        @JvmStatic
        fun getInstance(context: Context): BusConfig {
            return BusConfig(context)
        }
    }

    private val mContext = context.applicationContext
    private val mPref: SharedPreferences = context.getSharedPreferences("bus_config", Context.MODE_PRIVATE)

    fun getBusStopLastSyncTime(): Long {
        return mPref.getLong(LAST_BUS_STOPS_SYNC_TIME, 0)
    }

    fun setBusStopLastSyncTime(time: Long) {
        mPref.edit().putLong(LAST_BUS_STOPS_SYNC_TIME, time).apply()
    }

    fun getBusRouteLastSyncTime(): Long {
        return mPref.getLong(LAST_BUS_ROUTES_SYNC_TIME, 0)
    }

    fun setBusRouteLastSyncTime(time: Long) {
        mPref.edit().putLong(LAST_BUS_ROUTES_SYNC_TIME, time).apply()
    }

    fun getBusDataExpiryTime(): Long {
        val time = mPref.getLong(BUS_DATA_EXPIRY_TIME, 0L)
        if (time == 0L) {
            return mContext.resources.getInteger(R.integer.bus_data_expiry_time_ms).toLong()
        }
        return time
    }

    fun setBusDataExpiryTime(time: Long) {
        mPref.edit().putLong(BUS_DATA_EXPIRY_TIME, time).apply()
    }

    fun getBusStopSearchRange(): Int {
        val range = mPref.getInt(BUS_STOP_SEARCH_RANGE, 0)
        if (range == 0) {
            return mContext.resources.getInteger(R.integer.bus_stops_search_range)
        }
        return range
    }

    fun setBusStopSearchRange(range: Int) {
        mPref.edit().putInt(BUS_STOP_SEARCH_RANGE, range).apply()
    }

    fun getMinMoveToUpdate(): Int {
        val minMove = mPref.getInt(LOCATION_MIN_MOVE_TO_UPDATE, 0)
        if (minMove == 0) {
            return mContext.resources.getInteger(R.integer.location_min_movement_to_update)
        }
        return minMove
    }

    fun setMinMoveToUpdate(minMove: Int) {
        mPref.edit().putInt(LOCATION_MIN_MOVE_TO_UPDATE, minMove).apply()
    }
}