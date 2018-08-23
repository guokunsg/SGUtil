package com.gk.sgutil.bus.model

import android.content.Context
import com.gk.sgutil.util.Logger
import com.gk.sgutil.util.SingleSyncTask
import io.reactivex.Observable
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

// The maximum count of records is allowed.
// Just to prevent loop forever in case there is something wrong with the server
private const val MAX_COUNT_GUARD = 40000

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

/**
 * Class to download bus data from remote and manage the data locally
 */
class BusDataManager @Inject constructor(context: Context, db: BusDatabase, dataService: BusDataService) {

    private val mContext: Context = context.applicationContext
    private val mDb: BusDatabase = db
    private val mBusDataService: BusDataService = dataService

    private val mBusStopDao = BusStopDaoWithCache(db.busStopDao())
    private val mBusRouteDao = db.busRouteDao()
    // Some constant RX object to return
    private val mBusStopDaoRx = Observable.just(mBusStopDao as BusStopDao)
    private val mBusRouteDaoRx = Observable.just(mBusRouteDao)
    private val mNoSyncRx = Observable.just(0)

    /**
     * Get BusStopDao for database operations
     */
    fun getBusStopDao(): BusStopDao {
        return mBusStopDao
    }

    /**
     * Get BusRouteDao for database operations
     */
    fun getBusRouteDao(): BusRouteDao {
        return mBusRouteDao
    }

    /**
     * Allows synchronize first and then return BusStopDao
     */
    fun getBusStopDao(syncOption: SyncOption): Observable<BusStopDao> {
        if (needToSync(syncOption) { BusConfig.getInstance(mContext).getBusStopLastSyncTime() }) {
            return mBusStopSyncTask.runSyncData().map { mBusStopDao }
        } else {
            return mBusStopDaoRx
        }
    }

    /**
     * Allows synchronize first and then return BusRouteDao
     */
    fun getBusRouteDao(syncOption: SyncOption): Observable<BusRouteDao> {
        if (needToSync(syncOption) { BusConfig.getInstance(mContext).getBusRouteLastSyncTime()}) {
            return mBusRouteSyncTask.runSyncData().map { mBusRouteDao }
        } else {
            return mBusRouteDaoRx
        }
    }

    // Check whether need to do a data synchronization with the input option
    private fun needToSync(option: SyncOption, fnGetLastSyncTime: ()-> Long): Boolean {
        return when(option) {
            SyncOption.Force -> true
            SyncOption.AutoManage -> {
                val expiryTime = BusConfig.getInstance(mContext).getBusDataExpiryTime()
                return Math.abs(System.currentTimeMillis() - fnGetLastSyncTime()) > expiryTime
            }
            SyncOption.IfNoData ->
                return fnGetLastSyncTime() == 0L
        }
    }

    /**
     * Data synchronization options
     */
    enum class SyncOption {
        /** Do synchronization if data is retrieved more than one week */
        AutoManage,
        /** Force to do a synchronization */
        Force,
        /** Do the synchronization if there is no data */
        IfNoData
    }

    /**
     * Synchronize bus stops data with LTA server in a background thread
     * If there is already one synchronization currently running,
     * no new sync will start and the result from that sync is returned.
     * The sync starts without Observable subscribed
     * @return
     *      The number of records received.
     */
    fun syncBusStopsData(option: SyncOption): Observable<Int> {
        if (! needToSync(option) {BusConfig.getInstance(mContext).getBusStopLastSyncTime()})
            return mNoSyncRx
        return mBusStopSyncTask.runSyncData()
    }

    /**
     * Synchronize bus routes data with LTA server in a background thread
     * If there is already one synchronization currently running,
     * no new sync will start and the result from that sync is returned
     * The sync starts without Observable subscribed
     * @return
     *      The number of records received.
     *      It may be different from the number of records in the database as there are duplications
     */
    fun syncBusRoutesData(option: SyncOption): Observable<Int> {
        if (! needToSync(option) {BusConfig.getInstance(mContext).getBusRouteLastSyncTime()})
            return mNoSyncRx

        return mBusRouteSyncTask.runSyncData()
    }

    /**
     * Synchronize all the data. Synchronization is done in background threads
     */
    fun startSync(option: SyncOption) {
        // Don't have to be subscribed
        syncBusStopsData(option)
        syncBusRoutesData(option)
    }

    /**
     * The task to synchronize data with server.
     * Due to LTA service API limitation, all the data has to be retrieved and saved.
     */
    private val mBusStopSyncTask = object : SingleSyncTask<Int>() {
        override fun syncData(): Int {
            /**
             * Sync routine:
             * 1. Data fetching: Server each time only returns a number of data records,
             *    next time skip the total number of records received so far to continue,
             *    until there is no data returned
             * 2. Set the sync time to each data record and save to database
             * 3. Delete all the records in the database with a different timestamp to
             *    clear old data which are not available in new data
             */
            val syncTime = System.currentTimeMillis() // Synchronization time
            val allStops = ArrayList<BusStop>() // To store the received data
            while (allStops.size < MAX_COUNT_GUARD) {
                val busStops = mBusDataService.getBusStops(allStops.size).execute().body()!!.busStops
                if (busStops == null || busStops.isEmpty()) {
                    break
                } else {
                    // Update the synchronization time
                    for (busStop in busStops)
                        busStop.syncTime = syncTime
                    allStops.addAll(busStops)
                    Logger.debug("Bus stop sync. Received=${busStops.size} Total received=${allStops.size}")
                }
            }
            Logger.debug("Bus stop sync completed. Total=${allStops.size} Time used=${System.currentTimeMillis() - syncTime}")

            val dao = mDb.busStopDao()
            val busStops = allStops.toTypedArray()
            // Save at once and delete old data which has a different sync time
            dao.upsert(busStops)
            dao.deleteIfSyncTimeNotEqual(syncTime)

            BusConfig.getInstance(mContext).setBusStopLastSyncTime(syncTime)
            Logger.debug("Total time: ${System.currentTimeMillis() - syncTime}")

            return busStops.size
        }
    }

    /**
     * The task to run bus stop synchronization. See {@link #mBusStopSyncTask} for similar implementation
     */
    private val mBusRouteSyncTask = object : SingleSyncTask<Int>() {
        override fun syncData(): Int {
            val syncTime = System.currentTimeMillis()
            val allRoutes = ArrayList<BusRoute>()
            while (allRoutes.size < MAX_COUNT_GUARD) {
                val busRoutes = mBusDataService.getBusRoutes(allRoutes.size).execute().body()!!.routes
                if (busRoutes == null || busRoutes.isEmpty()) {
                    break
                } else {
                    // Update the synchronization time
                    for (busRoute in busRoutes)
                        busRoute.syncTime = syncTime
                    allRoutes.addAll(busRoutes)
                    Logger.debug("Bus routes sync. Received=${busRoutes.size} Total received: ${allRoutes.size}")
                }
            }
            Logger.debug("Bus route sync completed. Total=${allRoutes.size} Time used=${System.currentTimeMillis() - syncTime}")

            val dao = mDb.busRouteDao()
            val busRoutes = allRoutes.toTypedArray()
            // Save and delete old data which has a different sync time
            dao.upsert(busRoutes)
            dao.deleteIfSyncTimeNotEqual(syncTime)

            BusConfig.getInstance(mContext).setBusRouteLastSyncTime(syncTime)
            Logger.debug("Total time: ${System.currentTimeMillis() - syncTime}")
            return busRoutes.size
        }
    }
}


