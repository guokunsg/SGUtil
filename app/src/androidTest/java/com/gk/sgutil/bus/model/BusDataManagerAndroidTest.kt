package com.gk.sgutil.bus.model

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.gk.sgutil.bus.clearData
import com.gk.sgutil.bus.createInMemoryBusDatabase
import com.gk.sgutil.dagger.module.BusModelModule
import com.gk.test.DOUBLE_COMPARE_DELTA
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 */
@RunWith(AndroidJUnit4::class)
class BusDataManagerAndroidTest {

    @Test
    fun testLoadData() {
        val context = InstrumentationRegistry.getTargetContext()
        val manager = BusDataManager(context, createInMemoryBusDatabase(context), BusModelModule.provideBusDataService())
        val busStops = manager.getBusStopDao().loadAll()

        assertEquals("Expect no data", 0, busStops.size)
    }

    @Test
    fun testSyncBusStopData() {
        val context = InstrumentationRegistry.getTargetContext()
        clearData(context)
        val db = createInMemoryBusDatabase(context)
        val manager = BusDataManager(context, db, BusModelModule.provideBusDataService())

        val count = manager.syncBusStopsData(BusDataManager.SyncOption.IfNoData).blockingFirst()
        assertTrue("Expect received data count: $count > 4900", count > 4900) // Currently has more than 4900 bus stops
        val busStops = manager.getBusStopDao().loadAll()
        assertTrue(busStops.size == count)
        for (busStop in busStops) {
            assertNotEquals("Expect latitude not 0", 0.0, busStop.latitude, DOUBLE_COMPARE_DELTA)
            assertNotEquals("Expect longitude not 0", 0.0, busStop.longitude, DOUBLE_COMPARE_DELTA)
        }

        // Auto manage sync should not sync
        val count2 = manager.syncBusStopsData(BusDataManager.SyncOption.AutoManage).blockingFirst()
        assertTrue("Expect no sync is done as it has been synced in a short time", count2 == 0)

        // Force sync
        val count3 = manager.syncBusStopsData(BusDataManager.SyncOption.Force).blockingFirst()
        assertEquals("Expect the sync has the same count of data records", count, count3) // Usually should be the same
    }

    @Test
    fun testSyncBusRoutesData() {
        val context = InstrumentationRegistry.getTargetContext()
        clearData(context)
        val db = createInMemoryBusDatabase(context)
        val manager = BusDataManager(context, db, BusModelModule.provideBusDataService())

        val count = manager.syncBusRoutesData(BusDataManager.SyncOption.IfNoData).blockingFirst()
        assertTrue("Expect received data count: $count > 20000", count > 20000) // Currently has more than 20000 bus routes

        // Auto manage sync should not sync
        val count2 = manager.syncBusRoutesData(BusDataManager.SyncOption.AutoManage).blockingFirst()
        assertTrue("Expect no sync is done as it has been synced in a short time", count2 == 0)

        // Force sync
        val count3 = manager.syncBusRoutesData(BusDataManager.SyncOption.Force).blockingFirst()
        assertEquals("Expect the sync has the same count of data records", count, count3) // Usually should be the same
    }
}
