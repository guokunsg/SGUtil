package com.gk.sgutil

import com.facebook.drawee.backends.pipeline.Fresco
import com.gk.sgutil.bus.model.BusDataManager
import com.gk.sgutil.dagger.ApplicationModule
import com.gk.sgutil.dagger.DaggerAppComponent
import com.gk.sgutil.util.Logger
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

/**
 * Main application
 */
class MainApplication: DaggerApplication() {

    @Inject
    lateinit var mDataManager: BusDataManager

    override fun onCreate() {
        super.onCreate()

        Fresco.initialize(this)
        // Start data synchronization in the background
        mDataManager.startSync(BusDataManager.SyncOption.AutoManage)
    }

    @Suppress("UNCHECKED_CAST")
    @Inject
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val appComponent = DaggerAppComponent.builder()
                .application(this)
                .build()
        return appComponent
    }
}