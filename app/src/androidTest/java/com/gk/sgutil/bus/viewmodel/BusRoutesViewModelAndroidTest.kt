package com.gk.sgutil.bus.viewmodel

import android.arch.lifecycle.Observer
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.gk.sgutil.MainActivity
import com.gk.test.TestResult
import com.gk.test.createMockedLifecycleOwner
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 */
@RunWith(AndroidJUnit4::class)
class BusRoutesViewModelAndroidTest {

    @get:Rule
    public var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    val mResult = TestResult<BusRoutesResult>()

    private fun runTest(serviceNo: String) {
        val context = InstrumentationRegistry.getTargetContext()
        val model = BusRoutesViewModel(mActivityRule.activity.application)
        val owner = createMockedLifecycleOwner()
        owner.changeToResumed()

        DaggerTestComponent.builder().build().inject(model)

        // Observe the result
        model.getBusRoutes().observe(owner, Observer {
            mResult.done(it, null)
        })

        model.findBusRoutes(serviceNo)
    }

    @Test
    fun testSearching() {
        // Fetch bus arrivals information. Must have internet
        val serviceNo = "161"
        runTest(serviceNo)
        mResult.waitForEvent(20000)
        assertNotNull(mResult.data)
        val busRoutes = mResult.data as BusRoutesResult
        assertEquals("Expect correct service no", serviceNo, busRoutes.serviceNo)
        assertTrue("Routine should be found", busRoutes.busRoutes1.isNotEmpty())
        assertTrue("Routine should be found", busRoutes.busRoutes2.isNotEmpty())
        assertTrue("Expect bus stop code to bus stop object map is built", busRoutes.stopsMap.isNotEmpty())
    }
}