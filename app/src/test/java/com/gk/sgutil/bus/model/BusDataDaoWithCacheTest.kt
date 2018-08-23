package com.gk.sgutil.bus.model

import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

/**
 *
 */
class BusDataDaoWithCacheTest {
    @Suppress("ReplaceArrayEqualityOpWithArraysEquals")
    @Test
    fun testBusStopCache() {
        val stop = BusStop()
        stop.busStopCode = "123456"
        val srcStops = arrayOf(BusStop())
        val mockedDao = Mockito.mock(BusStopDao::class.java)
        Mockito.`when`(mockedDao.loadAll()).thenReturn(srcStops)
        val dao = BusStopDaoWithCache(mockedDao)
        val busStops = dao.loadAll()
        Assert.assertTrue("Expect the same object returned from mocked dao", srcStops == busStops)
        val busStops2 = dao.loadAll() // Should return a clone of cached one
        Assert.assertTrue("Expect a cloned object is returned", busStops != busStops2)
        dao.upsert(arrayOf(stop)) // Cached should be invalid
        val busStops3 = dao.loadAll()
        Assert.assertTrue("Expect the object returned from mocked dao", srcStops == busStops3)
    }
}