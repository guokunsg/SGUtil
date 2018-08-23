package com.gk.sgutil.bus.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData

/**
 * Base ViewModel which provides searching progress LiveData
 */
open class BaseProgressViewModel(app: Application) : AndroidViewModel(app) {

    enum class ProgressStatus {
        Stopped,
        Searching,
    }

    /**
     * To update the current searching progress
     */
    class SearchingProgress(val status: ProgressStatus, val error: Throwable?) {
    }

    protected val mProgress: MutableLiveData<SearchingProgress> = MutableLiveData()

    /**
     * To observe the current searching progress
     */
    fun getProgress() : LiveData<SearchingProgress> {
        return mProgress
    }
}