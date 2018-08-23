package com.gk.sgutil.location

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.gk.test.DOUBLE_COMPARE_DELTA
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 */
@RunWith(AndroidJUnit4::class)
class AddressFinderTest {
    private val mContext = InstrumentationRegistry.getTargetContext()!!

    @Test
    fun testAddressFinder() {
        val location = LatLng(1.27066408655104,103.856977943394)
        val location2 = LatLng(1.357098686,103.902042)
        val location3 = LatLng(1.319535712,103.8750668)
        val finder = AddressFinder(mContext)
        val address = finder.getAddress(location)
        assertNull("No address yet", address)

        val addresses = finder.findAddresses(arrayOf(location, location2, location3)).blockingFirst()
        assertEquals(3, addresses.size)
        // Latitude and longitude not exactly the same
        //assertEquals(location.latitude, addresses[0]!!.latitude, DOUBLE_COMPARE_DELTA)
        //assertEquals(location.longitude, addresses[0]!!.longitude, DOUBLE_COMPARE_DELTA)
        //assertEquals(location2.latitude, addresses[1]!!.latitude, DOUBLE_COMPARE_DELTA)
        //assertEquals(location2.longitude, addresses[1]!!.longitude, DOUBLE_COMPARE_DELTA)
        //assertEquals(location3.latitude, addresses[2]!!.latitude, DOUBLE_COMPARE_DELTA)
        //assertEquals(location3.longitude, addresses[2]!!.longitude, DOUBLE_COMPARE_DELTA)
    }
}