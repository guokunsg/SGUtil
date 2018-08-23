package com.gk.sgutil.bus.model

import org.junit.After
import android.content.Context
import android.support.test.InstrumentationRegistry
import org.junit.Before
import android.support.test.runner.AndroidJUnit4
import com.gk.sgutil.bus.ASSERT_BUS_ROUTE_SAMPLE
import com.gk.sgutil.bus.ASSERT_BUS_STOP_SAMPLE
import com.gk.sgutil.bus.createInMemoryBusDatabase
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.io.InputStreamReader

/**
 *
 */
@RunWith(AndroidJUnit4::class)
class BusDatabaseAndroidTest {
    private var mContext: Context? = null
    private var mDb: BusDatabase? = null

    @Before
    fun createDb() {
        mContext = InstrumentationRegistry.getContext()
        mDb = createInMemoryBusDatabase(mContext!!)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        mDb!!.close()
    }

    @Test
    @Throws(Exception::class)
    fun testBusStopsDao() {
        val sample = loadBusStopSample()
        assertEquals(500, sample.size)

        // Set synchronization time
        var time = System.currentTimeMillis()
        setBusStopSyncTime(sample, time)

        val dao = mDb!!.busStopDao()

        // Save and load
        dao.upsert(sample)
        val loaded = dao.loadAll()
        assertEquals("Expect the same number of data is restored", sample.size, loaded.size)

        time += 100
        setBusStopSyncTime(loaded, time)

        // Save and load again. sync time should change
        dao.upsert(loaded)
        val loaded2 = dao.loadAll()
        assertEquals(500, loaded2.size)
        assertEquals("Expect syncTime is updated in all records", time, loaded2[0].syncTime)

        // Modify one sync time
        loaded[0].syncTime = time + 100
        dao.upsert(arrayOf(loaded[0]))
        // Delete the records which are not equal to time, one record should be deleted
        dao.deleteIfSyncTimeNotEqual(time)
        val loaded3 = dao.loadAll()
        assertEquals(loaded2.size - 1, loaded3.size)

        val busStops = dao.loadBusStop(arrayOf("01012", "01419"))
        assertEquals("Expect two results are returned", 2, busStops.size)
        assertEquals("01012", busStops[0].busStopCode)
        assertEquals("01419", busStops[1].busStopCode)
    }

    private fun loadBusStopSample(): Array<BusStop> {
        val gson = Gson()
        val stream = mContext!!.assets.open(ASSERT_BUS_STOP_SAMPLE)
        val reader = InputStreamReader(stream)
        try {
            return gson.fromJson<BusStop.BusStops>(
                    reader, BusStop.BusStops::class.java)!!.busStops!!
        } finally {
            reader.close()
        }
    }

    private fun setBusStopSyncTime(busStops: Array<BusStop>, time: Long) {
        for (stop in busStops)
            stop.syncTime = time
    }

    @Test
    @Throws(Exception::class)
    fun testBusRoutesDao() {
        val sample = loadBusRoutesSample()
        assertEquals(500, sample.size)

        // Set synchronization time
        var time = System.currentTimeMillis()
        setBusRouteSyncTime(sample, time)

        val dao = mDb!!.busRouteDao()

        // Save and load
        dao.upsert(sample)
        val loaded = dao.loadBusRoutes("10")
        assertEquals(148, loaded.size) // 148 stops in both directions

        time += 100
        setBusRouteSyncTime(loaded, time)

        // Save and load again. sync time should change
        dao.upsert(loaded)
        val loaded2 = dao.loadBusRoutes("10")
        assertEquals(148, loaded2.size)
        assertEquals(time, loaded2[0].syncTime)

        // Modify one sync time
        loaded[0].syncTime = time + 100
        dao.upsert(arrayOf(loaded[0]))
        // Delete the records which are not equal to time, one record should be deleted
        dao.deleteIfSyncTimeNotEqual(time)
        val loaded3 = dao.loadBusRoutes("10")
        assertEquals("Expect one record is deleted", loaded2.size - 1, loaded3.size)
    }

    private fun loadBusRoutesSample(): Array<BusRoute> {
        val gson = Gson()
        val stream = mContext!!.assets.open(ASSERT_BUS_ROUTE_SAMPLE)
        val reader = InputStreamReader(stream)
        try {
            return gson.fromJson<BusRoute.BusRoutes>(
                    reader, BusRoute.BusRoutes::class.java)!!.routes!!
        } finally {
            reader.close()
        }
    }

    private fun setBusRouteSyncTime(busRoutes: Array<BusRoute>, time: Long) {
        for (route in busRoutes) {
            route.syncTime = time
        }
    }
}
