package com.gk.sgutil.bus.model

import java.lang.ref.WeakReference

/**
 * Wraps BusStopDao with caching on all the data
 * Not exact thread safe but there should not be many threads running concurrently
 */
class BusStopDaoWithCache(private val mDao: BusStopDao) : BusStopDao {
    override fun upsert(busStops: Array<BusStop>) {
        mDao.upsert(busStops)
        setCache(null)
    }

    override fun loadAll(): Array<BusStop> {
        var busStops = getCache()
        if (busStops == null) {
            busStops = mDao.loadAll()
            setCache(busStops)
        }
        return busStops
    }

    override fun loadBusStop(stopCodes: Array<String>): Array<BusStop> {
        return mDao.loadBusStop(stopCodes)
    }

    override fun deleteIfSyncTimeNotEqual(syncTime: Long) {
        mDao.deleteIfSyncTimeNotEqual(syncTime)
        setCache(null)
    }

    companion object {
        @Volatile private var sCache: WeakReference<Array<BusStop>>? = null
    }

    private fun setCache(busStops: Array<BusStop>?) {
        sCache = if (busStops == null)
            null
        else
            WeakReference(busStops.clone())
    }

    private fun getCache(): Array<BusStop>? {
        val cached = sCache ?: return null
        val stops = cached.get()
        return if (stops == null) null else stops.clone()
    }
}