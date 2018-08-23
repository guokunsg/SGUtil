package com.gk.sgutil.location

import android.content.Context
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.gk.sgutil.SGUtilException
import com.gk.test.TestResult
import com.gk.test.TestUtils.Companion.randomSleep
import com.gk.test.TestUtils.Companion.runAsAsync
import com.gk.test.TestUtils.Companion.setPrivateField
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.reactivex.subjects.Subject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList

/**
 *
 */
@RunWith(MockitoJUnitRunner::class)
class LocationCollectorTest {

    @Mock
    private lateinit var mockContext: Context
    @Mock
    private lateinit var mockClient: FusedLocationProviderClient
    @Mock
    private lateinit var mockApiBuilder: GoogleApiClient.Builder
    @Mock
    private lateinit var mockApiClient: GoogleApiClient
    @Mock
    private lateinit var mockStartUpdateTask: Task<Void>
    @Mock
    private lateinit var mockLocationManager: LocationManager

    // The callbacks in LocationCollector to fire events
    private lateinit var mConnCallback : GoogleApiClient.ConnectionCallbacks
    private lateinit var mConnFailedCallback: GoogleApiClient.OnConnectionFailedListener
    @Volatile private lateinit var mLocationCallback: LocationCallback

    // Object to be tested
    private lateinit var mCollector: LocationCollector
    private val mResult = TestResult<Location>()

    @Suppress("UNCHECKED_CAST")
    @Before
    fun setup() {
        `when`(mockContext.applicationContext).thenReturn(mockContext)
        `when`(mockContext.resources).thenReturn(mock(Resources::class.java))
        // Mock ApiBuilder to receive the callbacks and pass back api client mock
        `when`(mockApiBuilder.addConnectionCallbacks(any(GoogleApiClient.ConnectionCallbacks::class.java)))
                .thenAnswer {
                    mConnCallback = it.arguments.get(0) as GoogleApiClient.ConnectionCallbacks
                    mockApiBuilder
                }
        `when`(mockApiBuilder.addOnConnectionFailedListener(any(GoogleApiClient.OnConnectionFailedListener::class.java)))
                .thenAnswer {
                    mConnFailedCallback = it.arguments.get(0) as GoogleApiClient.OnConnectionFailedListener
                    mockApiBuilder
                }
        `when`(mockApiBuilder.addApi(any())).thenReturn(mockApiBuilder)
        `when`(mockApiBuilder.build()).thenReturn(mockApiClient)
        // Mock location client
        `when`(mockClient.requestLocationUpdates(any(LocationRequest::class.java), any(LocationCallback::class.java), any(Looper::class.java)))
                .thenAnswer{
                    mLocationCallback = it.arguments.get(1) as LocationCallback
                    mockStartUpdateTask
                }
        // Mock the task returned by location client
        `when`(mockStartUpdateTask.addOnCompleteListener(any())).thenAnswer {
                    runAsAsync {
                        randomSleep()
                        (it.arguments.get(0) as OnCompleteListener<Void>).onComplete(mockStartUpdateTask)
                    }
                    mockStartUpdateTask
                }
        // Mock LocationManager
        `when`(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLocationManager)

        mCollector = LocationCollector.newInstanceForTesting(mockContext, mockClient, mockApiBuilder)
        // Update private fields to mock Android dependent calls
        setPrivateField(mCollector, FN_PERMISSION_CHECKER, ::alwaysHasPermission)
        setPrivateField(mCollector, FN_GET_LOOPER, ::getLooper)
    }

    /**
     * Let the collector start location update.
     */
    private fun startUpdate() : Subject<Location> {
        val subject = mCollector.startLocationUpdate()
        subject.subscribe({
            mResult.done(it, null)
        }, {
            mResult.done(null, it)
        })
        return subject
    }

    @Test
    fun testApiConnectionFailed() {
        startUpdate()
        // Testing: Simulate connection failed
        runAsAsync {
            mConnFailedCallback.onConnectionFailed(ConnectionResult(ConnectionResult.SERVICE_MISSING))
        }
        mResult.waitForEvent()
        assertNull(mResult.data)
        assertEquals("Expect report location service error message",
                SGUtilException.ErrorCode.LocationServiceError, (mResult.error as SGUtilException).errCode)
    }

    @Test
    fun testNoPermission() {
        // Testing: Simulate no permission
        setPrivateField(mCollector, FN_PERMISSION_CHECKER, ::alwaysNoPermission)
        startUpdate()
        runAsAsync{ mConnCallback.onConnected(null) }

        mResult.waitForEvent()
        assertNull(mResult.data)
        assertEquals("Expect no location permission error",
                SGUtilException.ErrorCode.NoLocationPermission, (mResult.error as SGUtilException).errCode)
    }

    @Test
    fun testNormalFlow() {
        val location = mock(Location::class.java)
        val locations = LocationResult.create(Arrays.asList(location))
        startUpdate()
        runAsAsync { mConnCallback.onConnected(null) }
        runAsAsync {
            randomSleep(100, 200)
            mLocationCallback.onLocationResult(locations) // Pass the location
        }
        mResult.waitForEvent()
        assertNull(mResult.error)
        assertTrue("Expect the location object is returned", location == mResult.data)
    }

    @Test
    fun testStartUpdateTaskFailed() {
        // Simulate location service request for update failed
        startUpdate()
        runAsAsync { mConnCallback.onConnected(null) }
        `when`(mockStartUpdateTask.getException()).thenReturn(RuntimeException("DummyError"))

        mResult.waitForEvent()
        assertNull(mResult.data)
        assertEquals("Expect report location service error message",
                SGUtilException.ErrorCode.LocationServiceError, (mResult.error as SGUtilException).errCode)
    }

    @Test
    fun testMultipleUpdate() {
        // Simulate multiple updates
        mResult.latch = CountDownLatch(3)
        val subject = startUpdate()
        runAsAsync { mConnCallback.onConnected(null) }
        var location = mock(Location::class.java)
        val locations = ArrayList<Location>()
        // Return last location
        `when`(mockLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(location)
        runAsAsync {
            // Update location twice
            randomSleep()
            locations.add(mock(Location::class.java))
            mLocationCallback.onLocationResult(LocationResult.create(locations)) // Pass the location
            randomSleep()
            location = mock(Location::class.java) // Save the location
            locations.add(location)
            mLocationCallback.onLocationResult(LocationResult.create(locations)) // Pass the location
        }
        mResult.waitForEvent()
        assertEquals("Expect calls count down latch to 0", 0, mResult.latch.count)
        if (mResult.error != null)
            mResult.error!!.printStackTrace()
        assertNull(mResult.error)
        assertTrue("Expect location data object is returned", location == mResult.data)

        // subscribe. should be updated
        subject.subscribe{
            location = it
        }
        Thread.sleep(50)
        assertTrue("Expect new subscription receive the location", location == mResult.data)
    }
}