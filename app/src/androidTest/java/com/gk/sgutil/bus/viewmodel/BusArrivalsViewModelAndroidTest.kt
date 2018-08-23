package com.gk.sgutil.bus.viewmodel

import android.arch.lifecycle.Observer
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.gk.sgutil.MainActivity
import com.gk.sgutil.bus.model.BusDataManager
import com.gk.sgutil.dagger.module.BusModelModule
import com.gk.test.TestResult
import com.gk.test.createMockedLifecycleOwner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 */
@RunWith(AndroidJUnit4::class)
class BusArrivalsViewModelAndroidTest {

    @get:Rule
    public var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    val mResult = TestResult<BusArrivals>()

    private fun runTest(stopCode: String) {
        val context = InstrumentationRegistry.getTargetContext()

        val model = BusArrivalsViewModel(mActivityRule.activity.application)
        val owner = createMockedLifecycleOwner()
        owner.changeToResumed()

        DaggerTestComponent.builder().build().inject(model)

        // Observe the result
        model.getBusArrivals().observe(owner, Observer {
            mResult.done(it, null)
        })

        val busStop = BusDataManager(
                context, BusModelModule.provideBusDatabase(context), BusModelModule.provideBusDataService())
                .getBusStopDao().loadBusStop(arrayOf(stopCode)).get(0)
        model.findBusArrivals(busStop)
    }

    @Test
    fun testFetching() {
        // Fetch bus arrivals information. Must have internet
        val stopCode = "46009"
        runTest(stopCode)
        mResult.waitForEvent(60000)
        assertNotNull("Expect data is received form network", mResult.data)
        val arrivals = mResult.data as BusArrivals
        assertEquals("Expect the data is for the input bus stop", stopCode, arrivals.busStop.busStopCode)
    }
}