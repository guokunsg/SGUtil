package com.gk.sgutil.bus.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.gk.sgutil.SGUtilException
import com.gk.sgutil.bus.model.BusDataManager
import com.gk.sgutil.bus.model.BusRoute
import com.gk.sgutil.bus.model.BusStop
import com.gk.sgutil.bus.model.BusStopDao
import com.gk.sgutil.util.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Routes information for a bus service
 * @param serviceNo
 *      Bus service number
 * @param busRoutes1
 *      Bus routes direction 1
 * @param busRoutes2
 *      Bus routes direction 2
 * @param stopsMap
 *      The map from bus stop code to bus stop data entity to provide bus stop information
 */
class BusRoutesResult(val serviceNo: String,
                      val busRoutes1: Array<BusRoute>,
                      val busRoutes2: Array<BusRoute>,
                      val stopsMap: Map<String, BusStop>) {
}

/**
 * Provides routes LiveData for a bus service
 */
class BusRoutesViewModel @Inject constructor(app: Application) : BaseProgressViewModel(app) {

    @Inject
    lateinit var mBusDataManager: BusDataManager

    private val mBusRoutes: MutableLiveData<BusRoutesResult> = MutableLiveData()

    fun getBusRoutes(): LiveData<BusRoutesResult> {
        return mBusRoutes
    }

    /**
     * Find bus route
     * @param serviceNo
     *      The bus service number
     */
    fun findBusRoutes(serviceNo: String) {
        mBusDataManager.getBusRouteDao(BusDataManager.SyncOption.IfNoData)
                .map { it.loadBusRoutes(serviceNo) }
                .map { buildResult(serviceNo, it, mBusDataManager) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    mBusRoutes.value = result
                    mProgress.value = SearchingProgress(ProgressStatus.Stopped, null)
                }, {
                    Logger.error("Error when getting data", it)
                    mProgress.value = SearchingProgress(ProgressStatus.Stopped, SGUtilException.translateError(it))
                })
        mProgress.postValue(SearchingProgress(ProgressStatus.Searching, null))
    }

    /**
     * Get the bus stops stored in the database with the input stop code
     * @param stopCodes:
     *      Contains the bus stop codes to be searched for
     * @return
     *      A map which contains bus stop code to bus stop data entity
     */
    private fun buildBusStopMap(stopCodes: Array<String>, dao: BusStopDao): Map<String, BusStop> {
        val stops = dao.loadBusStop(stopCodes)
        val map = android.util.ArrayMap<String, BusStop>()
        for (stop in stops) {
            map[stop.busStopCode] = stop
        }
        return map
    }

    // Generate result
    private fun buildResult(serviceNo: String, busRoutes: Array<BusRoute>, manager: BusDataManager): BusRoutesResult {
        // Get the map from bus stop code to bus stop
        val list = ArrayList<String>()
        for (route in busRoutes)
            list.add(route.busStopCode!!)
        val map = buildBusStopMap(list.toTypedArray(), manager.getBusStopDao())

        // Build routes for two directions
        val routes1 = ArrayList<BusRoute>()
        val routes2 = ArrayList<BusRoute>()
        for (route in busRoutes) {
            if (route.direction == 1) {
                routes1.add(route)
            } else if (route.direction == 2) {
                routes2.add(route)
            } else {
                Logger.error("Third route direction found for service $serviceNo!!!")
            }
        }
        return BusRoutesResult(serviceNo,
                routes1.sortedWith(compareBy { it.stopSequence }).toTypedArray(),
                routes2.sortedWith(compareBy { it.stopSequence }).toTypedArray(),
                map)
    }
}
