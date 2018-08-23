package com.gk.sgutil.bus.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.location.Location
import com.gk.sgutil.SGUtilException
import com.gk.sgutil.bus.model.BusConfig
import com.gk.sgutil.bus.model.BusDataManager
import com.gk.sgutil.bus.model.BusStop
import com.gk.sgutil.location.LocationCollector
import com.gk.sgutil.util.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * To store the bus stop information near a location
 * @param distance
 *      The distance of this bus stop to the location
 * @param busStop
 *      Bus stop data entity
 */
class BusStopNearbyInfo(
        val distance: Double,
        val busStop: BusStop) {
}

/**
 * Bus stops near a locations
 * @param location:
 *      The location which bus stops are near by
 * @param busStops:
 *      The bus stops being found
 * @param range:
 *      All the distances of bus stops are within this range
 */
class BusStopsNearby(val location: Location,
                     val busStops: Array<BusStopNearbyInfo>,
                     val range: Int) {
}

/**
 * The ViewModel which contains the live data for searching bus stops near the phone location.
 * When fragment starts, call startSearching() to start collecting location and find bus stops nearby.
 * When fragment stops, call stopSearching() to stop updating.
 * Observe on LiveData returned by getBusStopsNearby() to receive the latest update
 */
class BusStopNearbyViewModel(app: Application) : BaseProgressViewModel(app) {

    @Inject
    lateinit var mDataManager: BusDataManager
    @Inject
    lateinit var mLocationCollector: LocationCollector

    @Inject
    constructor(app: Application, dataManager: BusDataManager, collector: LocationCollector) : this(app) {
        mDataManager = dataManager
        mLocationCollector = collector
    }

    // These functions can be replaced for testing purpose
    private val fnAndroidSchedulersMainThread = AndroidSchedulers::mainThread
    private val fnSearchInTheRange = ::searchInTheRange

    // Live data
    private var mBusStops = MutableLiveData<BusStopsNearby>()
    private var mError = MutableLiveData<Throwable>()

    private var mLocationDisposable: Disposable? = null
    private var mLastLocation: Location? = null

    /**
     * To observe the bus stops which have been found.
     */
    fun getBusStopsNearby(): LiveData<BusStopsNearby> {
        return mBusStops
    }

    /**
     * To observe the error occurred
     */
    fun getError(): LiveData<Throwable> {
        return mError
    }

    /**
     * Start to collection locations and search for bus stops nearby.
     * Call this when view start and call stop when view stops
     */
    fun startLocationCollection() {
        mLastLocation = null
        // Already started
        if (mLocationDisposable != null)
            return

        mLocationDisposable = mLocationCollector.startLocationUpdate()
                .subscribeOn(Schedulers.io())
                .subscribe ({
                    val config = BusConfig.getInstance(getApplication())
                    val range = config.getBusStopSearchRange()
                    val minMove = config.getMinMoveToUpdate()
                    // Only search if location chagne is above a distance
                    if (mLastLocation == null ||
                            mLastLocation!!.distanceTo(it!!) > minMove) {
                        findBusStopsNearby(it, range)
                    }
                }, {
                    mError.value = it
                })
    }

    /**
     * Stop getting locations and computation
     */
    fun stopLocationCollection() {
        mLocationCollector.stopLocationUpdate()

        if (mLocationDisposable != null) {
            mLocationDisposable!!.dispose()
            mLocationDisposable = null
        }
    }

    /**
     * Find bus stops nearby
     * @param location
     *      The location of the search center
     * @param range
     *      Find stops within this range
     */
    private fun findBusStopsNearby(location: Location, range: Int) {

        mDataManager.getBusStopDao(BusDataManager.SyncOption.IfNoData)
                .map { it.loadAll() }
                .subscribeOn(Schedulers.io())
                .map { fnSearchInTheRange(it, location, range) }
                .observeOn(fnAndroidSchedulersMainThread())
                .subscribe({
                    mBusStops.value = it
                    mLastLocation = it.location
                }, {
                    Logger.error("Error when searching data", it)
                    mError.value = SGUtilException.translateError(it)
                })
    }

    /**
     * Find all the bus stops within the range.
     * @param busStops
     *      All the bus stops
     * @param location
     *      The center location
     * @param range
     *      The distance of returned bus stop should be within this range
     */
    private fun searchInTheRange(busStops: Array<BusStop>, location: Location, range: Int): BusStopsNearby {
        Logger.debug("Searching in the range: Location=$location busStops: ${busStops.size}")
        val list = ArrayList<BusStopNearbyInfo>()
        val loc = Location("lta")
        for (stop in busStops) {
            loc.latitude = stop.latitude
            loc.longitude = stop.longitude
            // TODO: There might be a better way to filter out the locations too far away by comparing first,
            // distanceTo may use float computation and slow
            val dis = location.distanceTo(loc)
            if (dis < range)
                list.add(BusStopNearbyInfo(dis.toDouble(), stop))
        }
        Logger.debug("Found ${list.size} bus stops within $range meters of the location")
        return BusStopsNearby(location, list.sortedWith(compareBy { it.distance }).toTypedArray(), range)
    }
}
