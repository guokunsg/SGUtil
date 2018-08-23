package com.gk.sgutil.bus.viewmodel

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import android.content.res.Resources
import android.location.Location
import com.gk.sgutil.R
import com.gk.sgutil.bus.model.BusDataManager
import com.gk.sgutil.bus.model.BusStop
import com.gk.sgutil.bus.model.BusStopDao
import com.gk.sgutil.location.LocationCollector
import com.gk.sgutil.test_util.MemorySharedPreferences
import com.gk.sgutil.util.Logger
import com.gk.test.MockedLifecycleOwner
import com.gk.test.TestResult
import com.gk.test.TestUtils.Companion.setPrivateField
import com.gk.test.createMockedLifecycleOwner
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch


/**
 *
 */
// Ignore UnnecessaryStubbingException
@RunWith(MockitoJUnitRunner.Silent::class)
class BusStopNearbyViewModelTest {

    private val RANGE = 100
    private val MIN_MOVE = 10

    @get: Rule
    public var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockApp: Application
    @Mock
    private lateinit var mockResources: Resources
    private val mPref = MemorySharedPreferences()
    @Mock
    private lateinit var mockManager: BusDataManager
    @Mock
    private lateinit var mockCollector: LocationCollector
    @Mock
    private lateinit var mockBusStopDao : BusStopDao

    private lateinit var mModel : BusStopNearbyViewModel
    private lateinit var mOwner : MockedLifecycleOwner
    private val mResult = TestResult<BusStopsNearby>()

    private val STOP1 = createBusStop("1", 1.111, 1.111)
    private val STOP2 = createBusStop("1", 1.112, 1.112)
    private val STOP3 = createBusStop("1", 1.113, 1.113)
    private val mBusStops = arrayOf( STOP1, STOP2, STOP3 )

    private fun createBusStop(code: String, lat: Double, log: Double): BusStop {
        val busStop = BusStop()
        busStop.busStopCode = code
        busStop.latitude = lat
        busStop.longitude = log
        return busStop
    }

    private val mLocationRx = BehaviorSubject.create<Location>()

    @Before
    fun setup() {
        `when`(mockApp.resources).thenReturn(mockResources)
        `when`(mockApp.applicationContext).thenReturn(mockApp)
        `when`(mockApp.getSharedPreferences(anyString(), anyInt())).thenReturn(mPref)
        `when`(mockResources.getInteger(R.integer.bus_stops_search_range)).thenReturn(RANGE)
        `when`(mockResources.getInteger(R.integer.location_min_movement_to_update)).thenReturn(MIN_MOVE)
        `when`(mockCollector.startLocationUpdate()).thenReturn(mLocationRx)
        `when`(mockManager.getBusStopDao(BusDataManager.SyncOption.IfNoData)).thenReturn(Observable.just(mockBusStopDao))
        `when`(mockBusStopDao.loadAll()).thenReturn(mBusStops)

        mModel = BusStopNearbyViewModel(mockApp, mockManager, mockCollector)
        setPrivateField(mModel, "fnAndroidSchedulersMainThread", Schedulers::io)
        setPrivateField(mModel, "fnSearchInTheRange", ::searchInTheRange)
        mOwner = createMockedLifecycleOwner()
        mOwner.changeToResumed()
    }

    private val mCallCount = ArrayList<BusStopNearbyInfo>()
    // This function would replace the one inside the class
    @Suppress("UNUSED_PARAMETER")
    private fun searchInTheRange(busStops: Array<BusStop>, location: Location, range: Int): BusStopsNearby {
        mCallCount.add(mock(BusStopNearbyInfo::class.java))
        return BusStopsNearby(location, mCallCount.toTypedArray(), range)
    }

    // Start the collection and observe on the data
    private fun startCollection() {
        mModel.startLocationCollection()
        mModel.getBusStopsNearby().observe(mOwner, Observer {
            mResult.done(it, null)
        })
        mModel.getError().observe(mOwner, Observer {
            mResult.done(null, it)
        })
    }

    @Test
    fun testNormal() {
        startCollection()
        mLocationRx.onNext(mock(Location::class.java)) // Mock location is updated
        mResult.waitForEvent()
        assertNotNull("Expect data is updated", mResult.data)
        assertEquals("Expect the same range value", RANGE, mResult.data!!.range)
        assertEquals("Expect one update call", 1, mCallCount.size)
    }

    @Test
    fun testWithSmallMove() {
        // Two location changes. By default, location distanceTo return 0, so should trigger only 1 result
        startCollection()
        val loc1 = mock(Location::class.java)
        val loc2 = mock(Location::class.java)
        `when`(loc1.distanceTo(any(Location::class.java))).thenReturn((MIN_MOVE - 1).toFloat())
        `when`(loc2.distanceTo(any(Location::class.java))).thenReturn((MIN_MOVE - 1).toFloat())
        mLocationRx.onNext(loc1)
        Thread.sleep(500) // Time to process
        mLocationRx.onNext(loc2)
        mResult.waitForEvent()
        assertNotNull(mResult.data)
        assertEquals(RANGE, mResult.data!!.range)
        assertEquals("Expect only one update call as the move is small", 1, mCallCount.size)
    }

    @Test
    fun testWithLargeMove() {
        mResult.latch = CountDownLatch(2)
        startCollection()
        mModel.startLocationCollection()
        val loc1 = mock(Location::class.java)
        val loc2 = mock(Location::class.java)
        `when`(loc1.distanceTo(any(Location::class.java))).thenReturn((MIN_MOVE + 1).toFloat())
        `when`(loc2.distanceTo(any(Location::class.java))).thenReturn((MIN_MOVE + 1).toFloat())
        mLocationRx.onNext(loc1)
        Thread.sleep(300)
        mLocationRx.onNext(loc2)
        mResult.waitForEvent()
        assertNotNull(mResult.data)
        assertEquals("Expect two update calls as the move is large", 2, mCallCount.size)
    }

    @Test
    fun testStop() {
        mResult.latch = CountDownLatch(2)
        startCollection()
        mModel.startLocationCollection()
        val loc1 = mock(Location::class.java)
        val loc2 = mock(Location::class.java)
        `when`(loc1.distanceTo(any(Location::class.java))).thenReturn((MIN_MOVE + 1).toFloat())
        `when`(loc2.distanceTo(any(Location::class.java))).thenReturn((MIN_MOVE + 1).toFloat())
        mLocationRx.onNext(loc1)
        Thread.sleep(300)
        mModel.stopLocationCollection() // Stop it. Should not process the second location
        Thread.sleep(200)
        mLocationRx.onNext(loc2)
        mResult.waitForEvent()
        assertNotNull(mResult.data)
        assertEquals("Expect only one update call as location update stopped", 1, mCallCount.size)
        verify(mockCollector).stopLocationUpdate()
    }
}