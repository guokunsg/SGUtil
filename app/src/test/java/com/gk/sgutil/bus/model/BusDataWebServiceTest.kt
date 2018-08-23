package com.gk.sgutil.bus.model

import com.gk.sgutil.dagger.module.BusModelModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 *
 */
class BusDataWebServiceTest {

    private val mDataService = BusModelModule.provideBusDataService()

    @Test
    fun testBusStops() {
        val busStops = mDataService.getBusStops(0).execute().body()
        assertTrue(busStops?.busStops!!.isNotEmpty())
    }

    @Test
    fun testBusRoutes() {
        val busRoutes = mDataService.getBusRoutes(0).execute().body()
        assertTrue(busRoutes?.routes!!.isNotEmpty())
    }

    @Test
    fun testBusArrivals() {
        val busArrival = mDataService.getBusArrival("83139").execute().body()
        assertEquals("83139", busArrival?.busStopCode)
        assertTrue(busArrival?.services?.size!! > 0)

        val busArrival2 = mDataService.getBusArrival("00000").execute().body()
        assertEquals("00000", busArrival2?.busStopCode)
    }

    @Test
    fun testTrafficImages() {
        val images = mDataService.getTrafficImages().execute().body()
        assertTrue(images!!.value!!.isNotEmpty())
    }
}