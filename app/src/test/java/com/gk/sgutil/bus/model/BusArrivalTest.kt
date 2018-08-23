package com.gk.sgutil.bus.model

import com.gk.sgutil.bus.URL_BUS_ARRIVAL
import com.gk.sgutil.bus.openResourceFiles
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.InputStreamReader
import java.text.SimpleDateFormat

/**
 *
 */
class BusArrivalTest {
    val SAMPLE_FILE = "bus_arrival_sample.json"
    // JSON ISO8601 date format parser
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")

    fun downloadSample() {
        println(com.gk.sgutil.bus.downloadSample(URL_BUS_ARRIVAL + "?BusStopCode=83139"))
    }

    @Test
    fun testBusArrivalParsing() {
        val gson = Gson()
        val arrival = gson.fromJson<BusArrival>(
                InputStreamReader(openResourceFiles(SAMPLE_FILE)), BusArrival::class.java)
        assertNotNull(arrival)
        assertEquals("http://datamall2.mytransport.sg/ltaodataservice/\$metadata#BusArrivalv2/@Element", arrival.metadata)
        assertEquals("83139", arrival.busStopCode)

        val svc = arrival.services?.get(0)
        assertEquals("15", svc?.serviceNo)
        assertEquals("GAS", svc?.operator)
        svc?.nextBus?.apply {
            assertEquals("77009", originCode)
            assertEquals("77009", destinationCode)
            assertEquals("2018-07-26T19:44:27+08:00", estimatedArrival)
            assertEquals("1.3291116666666667", latitude)
            assertEquals("103.90524083333334", longitude)
            assertEquals("1", visitNumber)
            assertEquals("SEA", load)
            assertEquals("WAB", feature)
            assertEquals("SD", type)
        }
        svc?.nextBus2?.apply {
            assertEquals("77009", originCode)
            assertEquals("77009", destinationCode)
            assertEquals("2018-07-26T19:56:12+08:00", estimatedArrival)
            assertEquals("1.348215", latitude)
            assertEquals("103.92469716666666", longitude)
            assertEquals("1", visitNumber)
            assertEquals("SEA", load)
            assertEquals("WAB", feature)
            assertEquals("SD", type)
        }
        svc?.nextBus3?.apply {
            assertEquals("77009", originCode)
            assertEquals("77009", destinationCode)
            assertEquals("2018-07-26T20:11:11+08:00", estimatedArrival)
            assertEquals("1.3567408333333333", latitude)
            assertEquals("103.94586966666667", longitude)
            assertEquals("1", visitNumber)
            assertEquals("SEA", load)
            assertEquals("WAB", feature)
            assertEquals("SD", type)
        }
    }
}