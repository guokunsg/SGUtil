package com.gk.sgutil.bus.model

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

/** Database name */
const val DATABASE_NAME = "bus.db"

// Bus stops table
const val TABLE_BUS_STOPS = "bus_stops"
object BusStopsColumn {
    const val BUS_STOP_CODE = "bus_stop_code"
    const val ROAD_NAME = "road_name"
    const val DESCRIPTION = "description"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val SYNC_TIME = "sync_time"
}

// Bus routes table
const val TABLE_BUS_ROUTES = "bus_routes"
object BusRoutesColumn {
    const val SERVICE_NO = "service_no"
    const val OPERATOR = "operator"
    const val DIRECTION = "direction"
    const val STOP_SEQUENCE = "stop_sequence"
    const val BUS_STOP_CODE = "bus_stop_code"
    const val DISTANCE = "distance"
    const val WD_FIRST_BUS = "wd_first_bus"
    const val WD_LAST_BUS = "wd_last_bus"
    const val SAT_FIRST_BUS = "sat_first_bus"
    const val SAT_LAST_BUS = "sat_last_bus"
    const val SUN_FIRST_BUS = "sun_first_bus"
    const val SUN_LAST_BUS = "sun_last_bus"
    const val SYNC_TIME = "sync_time"
}

/**
 * Room DAO for BusStop table operations
 */
@Dao
interface BusStopDao {
    /**
     * Insert the bus stops data.
     * If there are duplicates, old record will be replaced
     */
    @Insert(onConflict = REPLACE)
    fun upsert(busStops: Array<BusStop>)

    /**
     * Load all the bus stops data.
     * Need all the data to find the stops within a distance from the current location to the bus stop.
     */
    @Query("SELECT * FROM $TABLE_BUS_STOPS")
    fun loadAll(): Array<BusStop>

    /**
     * Load the bus stop with the input bus stop code
     */
    @Query("SELECT * FROM $TABLE_BUS_STOPS WHERE ${BusStopsColumn.BUS_STOP_CODE} IN (:stopCodes)")
    fun loadBusStop(stopCodes: Array<String>): Array<BusStop>

    /**
     * Delete all the records whose syncTime is not equal to the input value
     */
    @Query("DELETE FROM $TABLE_BUS_STOPS WHERE ${BusStopsColumn.SYNC_TIME} != :syncTime")
    fun deleteIfSyncTimeNotEqual(syncTime: Long)
}

@Dao
interface BusRouteDao {

    /**
     * Insert the bus route data.
     * If there are duplicates, old record will be replaced
     */
    @Insert(onConflict = REPLACE)
    fun upsert(busRoutes: Array<BusRoute>)

    /**
     * Load the route information about a bus service number
     * @return
     *      Array of the bus route which contains all the bus stops the bus passes by
     */
    @Query("SELECT * FROM $TABLE_BUS_ROUTES WHERE ${BusRoutesColumn.SERVICE_NO} = :serviceNo")
    fun loadBusRoutes(serviceNo: String): Array<BusRoute>

    /**
     * Get the bus services which are available on the bus stop
     * @return
     *      Array of bus stop code string
     */
    @Query("SELECT DISTINCT ${BusRoutesColumn.SERVICE_NO} FROM $TABLE_BUS_ROUTES " +
            "WHERE ${BusRoutesColumn.BUS_STOP_CODE} = :busStopCode " +
            "ORDER BY CAST (${BusRoutesColumn.SERVICE_NO} AS INTEGER) ASC")
    fun loadBusServices(busStopCode: String): Array<String>

    /**
     * Delete all the records whose syncTime is not equal to the input value
     */
    @Query("DELETE FROM $TABLE_BUS_ROUTES WHERE ${BusRoutesColumn.SYNC_TIME} != :syncTime")
    fun deleteIfSyncTimeNotEqual(syncTime: Long)
}

/**
 * Room implementation of the bus database
 */
@Database(entities = [BusStop::class, BusRoute::class], version = 1)
abstract class BusDatabase : RoomDatabase() {

    abstract fun busStopDao(): BusStopDao

    abstract fun busRouteDao(): BusRouteDao
}