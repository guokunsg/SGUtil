package com.gk.sgutil.bus.model

import android.content.Context
import android.content.SharedPreferences
import com.gk.sgutil.R
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

/**
 * Wraps SharedPreferences with default settings
 */
class BusConfig @Inject constructor(context: Context) {

    companion object {
        // Public constants
        // Move the camera to Singapore when the map is ready
        val MAP_INIT_LOCATION = LatLng(1.327, 103.826)

        // Runtime
        private const val LAST_BUS_STOPS_SYNC_TIME = "last_bus_stops_sync_time"
        private const val LAST_BUS_ROUTES_SYNC_TIME = "last_bus_routes_sync_time"

        // Configurations which have default value from xml
        private const val BUS_DATA_EXPIRY_TIME = "bus_data_expiry_time"
        private const val BUS_STOP_SEARCH_RANGE = "bus_stops_search_range"
        private const val LOCATION_MIN_MOVE_TO_UPDATE = "location_min_move_to_update"

        private const val BUS_STOPS_NEARBY_LAST_VIEWED_TAB = "bus_stops_nearby_last_view_tab"

        /** Returns a new instance of the configuration. Not singleton but data is consistent. */
        @JvmStatic
        fun getInstance(context: Context): BusConfig {
            return BusConfig(context)
        }
    }

    private val mContext = context.applicationContext
    private val mPref: SharedPreferences = context.getSharedPreferences("bus_config", Context.MODE_PRIVATE)

    /** Returns the last synchronization time for bus stops data. */
    fun getBusStopLastSyncTime(): Long {
        return mPref.getLong(LAST_BUS_STOPS_SYNC_TIME, 0)
    }

    /** Set the last synchronization time for bus stops data. */
    fun setBusStopLastSyncTime(time: Long) {
        mPref.edit().putLong(LAST_BUS_STOPS_SYNC_TIME, time).apply()
    }

    /** Returns the last synchronization time for bus routes data. */
    fun getBusRouteLastSyncTime(): Long {
        return mPref.getLong(LAST_BUS_ROUTES_SYNC_TIME, 0)
    }

    /** Set the last synchronization time for bus routes data. */
    fun setBusRouteLastSyncTime(time: Long) {
        mPref.edit().putLong(LAST_BUS_ROUTES_SYNC_TIME, time).apply()
    }

    /** Returns the how long the synchronized data should be expired in milliseoncds.
     * Returns the value defined in R.integer.bus_data_expiry_time_ms by default. */
    fun getBusDataExpiryTime(): Long {
        val time = mPref.getLong(BUS_DATA_EXPIRY_TIME, 0L)
        if (time == 0L) {
            return mContext.resources.getInteger(R.integer.bus_data_expiry_time_ms).toLong()
        }
        return time
    }

    /** Set the how long the synchronized data should be expired in milliseoncds. */
    fun setBusDataExpiryTime(time: Long) {
        mPref.edit().putLong(BUS_DATA_EXPIRY_TIME, time).apply()
    }

    /** Get the range in meters to search for bus stops nearby. */
    fun getBusStopSearchRange(): Int {
        val range = mPref.getInt(BUS_STOP_SEARCH_RANGE, 0)
        if (range == 0) {
            return mContext.resources.getInteger(R.integer.bus_stops_search_range)
        }
        return range
    }

    /** Set the range in meters to search for bus stops nearby. */
    fun setBusStopSearchRange(range: Int) {
        mPref.edit().putInt(BUS_STOP_SEARCH_RANGE, range).apply()
    }

    /** Get the minimum movement in meters to trigger a new search. */
    fun getMinMoveToUpdate(): Int {
        val minMove = mPref.getInt(LOCATION_MIN_MOVE_TO_UPDATE, 0)
        if (minMove == 0) {
            return mContext.resources.getInteger(R.integer.location_min_movement_to_update)
        }
        return minMove
    }

    /** Set the minimum movement in meters to trigger a new search. */
    fun setMinMoveToUpdate(minMove: Int) {
        mPref.edit().putInt(LOCATION_MIN_MOVE_TO_UPDATE, minMove).apply()
    }

    /** Get the last viewed tab index in the bus nearby UI. */
    fun getBusStopsNearbyLastViewedTab(): Int {
        return mPref.getInt(BUS_STOPS_NEARBY_LAST_VIEWED_TAB, 0)
    }

    /** Set the last viewed tab index in the bus nearby UI. */
    fun setBusStopsNearbyLastViewedTab(index: Int) {
        mPref.edit().putInt(BUS_STOPS_NEARBY_LAST_VIEWED_TAB, index).apply()
    }
}