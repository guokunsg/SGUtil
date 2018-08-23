package com.gk.sgutil.bus.model

import android.content.Context
import android.content.res.Resources
import com.gk.sgutil.dagger.module.BusModelModule
import com.gk.sgutil.test_util.MemorySharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

// Simulate bus stop dao
open class MemBusStopDao : BusStopDao {

    var busStops: Array<BusStop> = arrayOf()

    override fun upsert(busStops: Array<BusStop>) {
        this.busStops = busStops
    }

    override fun loadAll(): Array<BusStop> {
        return this.busStops.clone()
    }

    override fun loadBusStop(stopCodes: Array<String>): Array<BusStop> {
        return arrayOf()
    }

    override fun deleteIfSyncTimeNotEqual(syncTime: Long) {
    }
}

open class MemBusRouteDao : BusRouteDao {
    var busRoutes: Array<BusRoute> = arrayOf()

    override fun upsert(busRoutes: Array<BusRoute>) {
        this.busRoutes = busRoutes.clone()
    }

    override fun loadBusRoutes(serviceNo: String): Array<BusRoute> {
        return arrayOf()
    }

    override fun loadBusServices(busStopCode: String): Array<String> {
        return arrayOf()
    }

    override fun deleteIfSyncTimeNotEqual(syncTime: Long) {
    }
}

/**
 *
 */
@RunWith(MockitoJUnitRunner::class)
class BusDataManagerTest {

    @Mock
    private lateinit var mockContext: Context
    @Mock
    private lateinit var mockResources: Resources

    private val mPref = MemorySharedPreferences()

    @Mock
    private lateinit var mockDb: BusDatabase
    private var mBusStopDao = MemBusStopDao()
    private var mBusRouteDao = MemBusRouteDao()

    private lateinit var mBusDataManager: BusDataManager

    @Before
    fun setup() {
        `when`(mockContext.applicationContext).thenReturn(mockContext)
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mPref)
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockResources.getInteger(anyInt())).thenReturn(1)
        `when`(mockDb.busStopDao()).thenReturn(mBusStopDao)
        `when`(mockDb.busRouteDao()).thenReturn(mBusRouteDao)

        mBusDataManager = BusDataManager(mockContext, mockDb, BusModelModule.provideBusDataService())
    }

    @Suppress("ReplaceArrayEqualityOpWithArraysEquals")
    @Test
    fun testGetBusStopRx() {
        // Never sync before
        val dao1 = mBusDataManager.getBusStopDao(BusDataManager.SyncOption.IfNoData).blockingFirst()
        assertNotNull(dao1)
        val busStops1 = mBusStopDao.busStops
        assertTrue("Expect data is retrieved", busStops1.isNotEmpty())
        assertTrue("Expect sync time is saved in preference", mPref.map.isNotEmpty())

        // Do it again
        val dao2 = mBusDataManager.getBusStopDao(BusDataManager.SyncOption.IfNoData).blockingFirst()
        assertNotNull(dao2)
        assertTrue("Expect the same dao object", dao1 == dao2)
        val busStops2 = mBusStopDao.busStops
        assertTrue("Expect the data is not updated as there should be no sync", busStops1 == busStops2)

        // Forced sync
        val dao3 = mBusDataManager.getBusStopDao(BusDataManager.SyncOption.Force).blockingFirst()
        assertNotNull(dao3)
        val busStops3 = mBusStopDao.busStops
        assertTrue("Expect the data is updated as there should be sync", busStops1 != busStops3) // Data should change
    }

    @Suppress("ReplaceArrayEqualityOpWithArraysEquals")
    @Test
    fun testGetBusRouteRx() {
        // Never sync before
        val dao1 = mBusDataManager.getBusRouteDao(BusDataManager.SyncOption.IfNoData).blockingFirst()
        assertNotNull(dao1)
        val busRoutes1 = mBusRouteDao.busRoutes
        assertTrue("Expect data is retrieved", busRoutes1.isNotEmpty())
        assertTrue("Expect sync time is saved in preference", mPref.map.isNotEmpty())

        // Do it again
        val dao2 = mBusDataManager.getBusRouteDao(BusDataManager.SyncOption.IfNoData).blockingFirst()
        assertNotNull(dao2)
        assertTrue("Expect the same dao object", dao1 == dao2)
        val busRoutes2 = mBusRouteDao.busRoutes
        assertTrue("Expect the data is not updated as there should be no sync", busRoutes1 == busRoutes2) // Data should not change

        // Forced sync
        val dao3 = mBusDataManager.getBusRouteDao(BusDataManager.SyncOption.Force).blockingFirst()
        assertNotNull(dao3)
        val busRoutes3 = mBusRouteDao.busRoutes
        assertTrue("Expect the data is updated as there should be sync", busRoutes1 != busRoutes3) // Data should change
    }

    @Test
    fun testBusStopDataMultiSync() {
        // Call synchronization several times, but should only one running
        val busStopDao = object : MemBusStopDao() {
            var count = 0 // To record how many times the function is called
            override fun upsert(busStops: Array<BusStop>) {
                super.upsert(busStops)
                count++
            }
        }
        `when`(mockDb.busStopDao()).thenReturn(busStopDao)

        val result = IntArray(10)
        for (i in 0 until result.size) {
            thread {
                result[i] = mBusDataManager.syncBusStopsData(BusDataManager.SyncOption.IfNoData).blockingFirst()
            }
        }
        val count = mBusDataManager.syncBusStopsData(BusDataManager.SyncOption.IfNoData).blockingFirst()
        Thread.sleep(500)
        assertEquals("Expect there should be only 1 sync which has been done", 1, busStopDao.count)
        for (i in 0 until result.size)
            assertEquals("Expect all threads receive the same update", count, result[i])
    }

    @Test
    fun testBusRouteDataMultiSync() {
        // Call synchronization several times, but should only one running
        val latch = CountDownLatch(1)
        val busRouteDao = object : MemBusRouteDao() {
            var count = 0 // To record how many times the function is called
            override fun upsert(busRoutes: Array<BusRoute>) {
                this.busRoutes = busRoutes.clone()
                count++
                latch.countDown()
            }
        }
        `when`(mockDb.busRouteDao()).thenReturn(busRouteDao)

        val result = IntArray(10)
        for (i in 0 until result.size) {
            thread {
                result[i] = mBusDataManager.syncBusRoutesData(BusDataManager.SyncOption.IfNoData).blockingFirst()
            }
        }
        val count = mBusDataManager.syncBusRoutesData(BusDataManager.SyncOption.IfNoData).blockingFirst()
        latch.await(3000, TimeUnit.MILLISECONDS)
        Thread.sleep(100)
        assertEquals("Expect there should be only 1 sync which has been done", 1, busRouteDao.count)
        for (i in 0 until result.size)
            assertEquals("Expect all threads receive the same update", count, result[i])
    }

    @Test
    fun testSyncAll() {
        mBusDataManager.startSync(BusDataManager.SyncOption.AutoManage)
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 30000) {
            Thread.sleep(500)
            if (mBusStopDao.busStops.isNotEmpty() && mBusRouteDao.busRoutes.isNotEmpty())
                break
        }
        assertTrue("Expect data is synced", mBusStopDao.busStops.isNotEmpty())
        assertTrue("Expect data is synced", mBusRouteDao.busRoutes.isNotEmpty())
    }
}
