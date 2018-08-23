package com.gk.sgutil.dagger.module

import android.content.Context
import com.gk.sgutil.location.AddressFinder
import com.gk.sgutil.location.LocationCollector
import com.gk.sgutil.util.Logger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 *
 */
@Module
class LocationModule {

    @Module
    companion object {
        @JvmStatic @Provides
        fun provideLocationCollector(context: Context): LocationCollector {
            return LocationCollector.newInstance(context)
        }

        @Singleton @JvmStatic @Provides
        fun provideAddressFinder(context: Context): AddressFinder {
            return AddressFinder(context)
        }
    }
}