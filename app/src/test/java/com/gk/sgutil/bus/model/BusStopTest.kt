package com.gk.sgutil.bus.model

import com.gk.sgutil.bus.URL_BUS_STOPS
import com.gk.sgutil.bus.openResourceFiles
import com.gk.test.DOUBLE_COMPARE_DELTA
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.InputStreamReader

/**
 *
 */
class BusStopTest {
    // Sample data
    val SAMPLE_FILE = "bus_stops_sample.json"

    fun downloadSample() {
        System.out.println(com.gk.sgutil.bus.downloadSample(URL_BUS_STOPS));
    }

    @Test
    fun testBusStopParsing() {
        val gson = Gson()
        val busstops = gson.fromJson<BusStop.BusStops>(
                InputStreamReader(openResourceFiles(SAMPLE_FILE)), BusStop.BusStops::class.java)

        assertNotNull(busstops)
        assertEquals("http://datamall2.mytransport.sg/ltaodataservice/\$metadata#BusStops", busstops.metadata)
        assertEquals(500, busstops.busStops?.size)

        val busstop = busstops.busStops?.get(0)
        busstop?.apply {
            assertEquals("00481", busStopCode)
            assertEquals("Woodlands Rd", roadName)
            assertEquals("BT PANJANG TEMP BUS PK", description)
            assertEquals(1.383764, latitude, DOUBLE_COMPARE_DELTA)
            assertEquals(103.7583, longitude, DOUBLE_COMPARE_DELTA)
        }
    }
}