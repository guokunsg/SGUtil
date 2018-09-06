package com.gk.sgutil.bus.model

import android.content.Context
import android.content.res.Resources
import com.gk.sgutil.R
import com.gk.sgutil.test_util.MemorySharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

/**
 *
 */
@RunWith(MockitoJUnitRunner::class)
class BusConfigTest {

    @Mock
    private lateinit var mockContext : Context
    @Mock
    private lateinit var mockResources : Resources

    private val mPref = MemorySharedPreferences()

    @Before
    fun setup() {
        `when`(mockContext.applicationContext).thenReturn(mockContext)
        `when`(mockContext.resources).thenReturn(mockResources)
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mPref)
    }

    @Test
    fun testBusDataExpiryTime() {
        `when`(mockResources.getInteger(R.integer.bus_data_expiry_time_ms)).thenReturn(1)
        val config = BusConfig.getInstance(mockContext)
        assertEquals(1, config.getBusDataExpiryTime())
        config.setBusDataExpiryTime(2)
        assertEquals(2, config.getBusDataExpiryTime())
    }

    @Test
    fun testBusStopSearchRange() {
        `when`(mockResources.getInteger(R.integer.bus_stops_search_range)).thenReturn(1)
        val config = BusConfig.getInstance(mockContext)
        assertEquals(1, config.getBusStopSearchRange())
        config.setBusStopSearchRange(2)
        assertEquals(2, config.getBusStopSearchRange())
    }

    @Test
    fun testMinMoveToUpdate() {
        `when`(mockResources.getInteger(R.integer.location_min_movement_to_update)).thenReturn(1)
        val config = BusConfig.getInstance(mockContext)
        assertEquals(1, config.getMinMoveToUpdate())
        config.setMinMoveToUpdate(2)
        assertEquals(2, config.getMinMoveToUpdate())
    }

    @Test
    fun testSyncTime() {
        val config = BusConfig.getInstance(mockContext)
        config.setBusStopLastSyncTime(1)
        assertEquals(1, config.getBusStopLastSyncTime())
        config.setBusRouteLastSyncTime(2)
        assertEquals(2, config.getBusRouteLastSyncTime())
    }

    @Test
    fun testBusStopsNearbyLastViewedTab() {
        val config = BusConfig.getInstance(mockContext)
        assertEquals(0, config.getBusStopsNearbyLastViewedTab())
        config.setBusStopsNearbyLastViewedTab(1)
        assertEquals(1, config.getBusStopsNearbyLastViewedTab())
    }
}