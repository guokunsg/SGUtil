package com.gk.sgutil.bus.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.gk.sgutil.SGUtilException
import com.gk.sgutil.bus.model.BusArrival
import com.gk.sgutil.bus.model.BusDataService
import com.gk.sgutil.bus.model.BusStop
import com.gk.sgutil.util.Logger
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Represent bus arrivals search result on the bus stop
 * @param busStop
 *      The bus stop which
 * @param services
 *      Bus services currently still in operation on the bus stop.
 */
class BusArrivals(
        val busStop: BusStop,
        val services: Array<BusArrival.BusService>) {
}

/**
 * ViewModel for bus arrivals information on a bus stop
 */
class BusArrivalsViewModel @Inject constructor(app: Application) : BaseProgressViewModel(app) {

    private val mBusArrivals: MutableLiveData<BusArrivals> = MutableLiveData()
    @Inject
    lateinit var mBusDataService: BusDataService

    /**
     * Get bus arrivals LiveData
     */
    fun getBusArrivals(): LiveData<BusArrivals> {
        return mBusArrivals
    }

    /**
     * Search on the web service provider for bus arrivals information.
     * @param busStop
     *      The bus stop to search for bus arrivals.
     */
    fun findBusArrivals(busStop: BusStop) {
        if (mProgress.value != null && mProgress.value!!.status == ProgressStatus.Searching)
            return

        Observable.create(ObservableOnSubscribe<BusArrivals> { emitter ->
            // Search on the internet. May take some time
            val busArrival = mBusDataService.getBusArrival(busStop.busStopCode!!).execute().body()!!
            // Sort by service number
            val sorted= busArrival.services!!.sortedBy {
                var busNo = it.serviceNo!!
                for (i in busNo.length - 1 downTo 0)
                    if (Character.isDigit(busNo[i])) {
                        busNo = busNo.substring(0, i + 1)
                        break
                    }
                Integer.parseInt(busNo)
            }.toTypedArray()
            val arrivals = BusArrivals(busStop, sorted)
            Logger.debug("Found ${arrivals.services.size} services on bus stop ${busStop.description}")
            emitter.onNext(arrivals)
            emitter.onComplete()
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mBusArrivals.value = it
                    mProgress.value = SearchingProgress(ProgressStatus.Stopped, null)
                }, {
                    Logger.error("Error when getting data", it)
                    mProgress.value = SearchingProgress(ProgressStatus.Stopped, SGUtilException.translateError(it))
                })
        mProgress.postValue(SearchingProgress(ProgressStatus.Searching, null))
    }
}
