package com.gk.sgutil.bus

import android.content.Context
import android.location.Location
import android.os.SystemClock
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.gk.sgutil.location.FN_PERMISSION_CHECKER
import com.gk.sgutil.location.LocationCollector
import com.gk.sgutil.location.VAR_LOCATION_CLIENT
import com.gk.sgutil.util.Logger
import com.gk.test.DOUBLE_COMPARE_DELTA
import com.gk.test.TestResult
import com.gk.test.TestUtils.Companion.getPrivateField
import com.gk.test.TestUtils.Companion.setPrivateField
import com.google.android.gms.location.FusedLocationProviderClient
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

/**
 */
@RunWith(AndroidJUnit4::class)
class LocationCollectorAndroidTest {

    private lateinit var mLocationClient : FusedLocationProviderClient
    private val mMockedLocation = Location("flp")

    @Suppress("UNUSED_PARAMETER")
    private fun alwaysHasPermission(c: Context, p: Array<String>): Boolean {
        mMockedLocation.latitude = 1.2345
        mMockedLocation.longitude = 5.4321
        mMockedLocation.accuracy = 1.0f
        mMockedLocation.time = System.currentTimeMillis()
        mMockedLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        mLocationClient.setMockMode(true)
        mLocationClient.setMockLocation(mMockedLocation)
        return true
    }

    /**
     * @Note: !!
     * http://android.xsoftlab.net/training/location/location-testing.html#TurnOnMockMode
     * To send mock locations to Location Services in mock mode, a test app must request
     * the permission android.Manifest.permission#ACCESS_MOCK_LOCATION.
     * In addition, you must enable mock locations on the test device using the option Enable mock locations.
     */
    //@Test
    fun testWithMockedLocation() {

        val context = InstrumentationRegistry.getTargetContext()

        val collector = LocationCollector.newInstance(context)

        val result = TestResult<Location>()
        result.latch = CountDownLatch(2)

        // To inject the mock location
        mLocationClient = getPrivateField(collector, VAR_LOCATION_CLIENT) as FusedLocationProviderClient
        setPrivateField(collector, FN_PERMISSION_CHECKER, ::alwaysHasPermission)

        collector.startLocationUpdate().subscribe({
            result.done(it, null)
        }, {
            result.done(null, it)
        })

        result.waitForEvent(120000)

        assertEquals(mMockedLocation.latitude, result.data!!.latitude, DOUBLE_COMPARE_DELTA)
        assertEquals(mMockedLocation.longitude, result.data!!.longitude, DOUBLE_COMPARE_DELTA)

        Logger.debug("Location: ${result.data} Error: ${result.error}")
    }

    @Test
    fun justAvoidNoTestSuiteError() {

    }
}
