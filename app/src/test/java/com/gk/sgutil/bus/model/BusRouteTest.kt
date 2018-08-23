package com.gk.sgutil.bus.model

import com.gk.sgutil.bus.URL_BUS_ROUTES
import com.gk.sgutil.bus.openResourceFiles
import com.gk.test.DOUBLE_COMPARE_DELTA
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.InputStreamReader

/**
 *
 */
class BusRouteTest {

    val SAMPLE_FILE = "bus_routes_sample.json"

    fun downloadSample() {
        System.out.println(com.gk.sgutil.bus.downloadSample(URL_BUS_ROUTES))
    }

    @Test
    fun testBusRoutesParsing() {
        //downloadSample()
        val gson = Gson()
        val routes = gson.fromJson<BusRoute.BusRoutes>(
                InputStreamReader(openResourceFiles(SAMPLE_FILE)), BusRoute.BusRoutes::class.java)
        assertEquals("http://datamall2.mytransport.sg/ltaodataservice/\$metadataBusRoutes", routes.metadata)
        assertEquals(500, routes.routes?.size)
        val route = routes.routes?.get(0)
        route?.apply {
            assertEquals("10", serviceNo)
            assertEquals("SBST", operator)
            assertEquals(1, direction)
            assertEquals(1, stopSequence)
            assertEquals("75009", busStopCode)
            assertEquals(0.0, distance, DOUBLE_COMPARE_DELTA)
            assertEquals("0500", wd_FirstBus)
            assertEquals("2300", wd_LastBus)
            assertEquals("0500", sat_FirstBus)
            assertEquals("2300", sat_LastBus)
            assertEquals("0500", sun_FirstBus)
            assertEquals("2300", sun_LastBus)
        }
    }
}