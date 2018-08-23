package com.gk.sgutil.bus.model

import com.gk.sgutil.bus.openResourceFiles
import com.gk.test.DOUBLE_COMPARE_DELTA
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import java.io.InputStreamReader

class TrafficImageTest {
    val SAMPLE_FILE = "traffic_images.json"

    @Test
    fun testParsing() {
        val images = Gson().fromJson<TrafficImage.TrafficImages>(
                InputStreamReader(openResourceFiles(SAMPLE_FILE)),
                TrafficImage.TrafficImages::class.java)
        assertNotNull(images.value)
        assertTrue(images.value!!.isNotEmpty())
        val image = images.value!![0]

        assertEquals("1001", image.cameraID)
        assertEquals(1.29531332, image.latitude, DOUBLE_COMPARE_DELTA)
        assertEquals(103.871146, image.longitude, DOUBLE_COMPARE_DELTA)
        assertEquals("https://s3-ap-southeast-1.amazonaws.com/mtpdm/2018-08-22/12-04/1001_1158_20180822120007_bd1eb2.jpg", image.imageLink)
    }
}