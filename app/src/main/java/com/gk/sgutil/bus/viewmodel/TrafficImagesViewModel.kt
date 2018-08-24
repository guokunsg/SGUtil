package com.gk.sgutil.bus.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.gk.sgutil.SGUtilException
import com.gk.sgutil.bus.model.BusDataService
import com.gk.sgutil.bus.model.TrafficImage
import com.gk.sgutil.util.Logger
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * The view model to download the real time traffic images from service provider
 */
class TrafficImagesViewModel @Inject constructor(app: Application) : BaseProgressViewModel(app) {

    private val mImages = MutableLiveData<Array<TrafficImage>>()
    @Inject
    lateinit var mBusDataService: BusDataService

    /**
     * Returns the traffic images live data for view to observe
     */
    fun getTrafficImages(): LiveData<Array<TrafficImage>> {
        return mImages
    }

    /**
     * Start data fetching
     */
    fun refreshImages() {
        if (mProgress.value != null && mProgress.value!!.status == ProgressStatus.Searching)
            return

        Observable.create(ObservableOnSubscribe<Array<TrafficImage>> { emitter ->
            val images = mBusDataService.getTrafficImages().execute().body()!!
            Logger.debug("Image info received with ${images.value!!.size} records")
            emitter.onNext(images.value!!)
            emitter.onComplete()
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mImages.value = it
                    mProgress.value = SearchingProgress(ProgressStatus.Stopped, null)
                }, {
                    Logger.error("Error when getting data", it)
                    mProgress.value = SearchingProgress(ProgressStatus.Stopped, SGUtilException.translateError(it))
                })
        mProgress.value = SearchingProgress(ProgressStatus.Searching, null)
    }
}