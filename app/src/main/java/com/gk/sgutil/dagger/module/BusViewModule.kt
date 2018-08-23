package com.gk.sgutil.dagger.module

import com.gk.sgutil.bus.model.BusStop
import com.gk.sgutil.bus.model.TrafficImage
import com.gk.sgutil.bus.view.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class BusViewModule {
    @ContributesAndroidInjector
    abstract fun contributeBusStopsNearbyFragment(): BusStopsNearbyFragment

    @ContributesAndroidInjector
    abstract fun contributeBusArrivalsFragment(): BusArrivalsFragment

    @ContributesAndroidInjector
    abstract fun contributeBusRoutesFragment(): BusRoutesFragment

    @ContributesAndroidInjector
    abstract fun contributeTrafficImagesFragment(): TrafficImagesFragment

    @ContributesAndroidInjector
    abstract fun contributeTrafficImageDialogFragment(): TrafficImageDialogFragment

    @Binds
    abstract fun bindBusStop(busStop: BusStop): BusStop

    @Binds
    abstract fun bindTrafficImage(trafficImage: TrafficImage): TrafficImage

    @Module
    companion object {
        @JvmStatic @Provides
        fun provideBusStopsNearbyFragment(): BusStopsNearbyFragment {
            return BusStopsNearbyFragment()
        }

        @JvmStatic @Provides
        fun provideBusArrivalsFragment(busStop: BusStop): BusArrivalsFragment {
            return BusArrivalsFragment.newInstance(busStop)
        }

        @JvmStatic @Provides
        fun provideBusRoutesFragment(busService: String): BusRoutesFragment {
            return BusRoutesFragment.newInstance(busService)
        }

        @JvmStatic @Provides
        fun provideTrafficImages(): TrafficImagesFragment {
            return TrafficImagesFragment()
        }

        @JvmStatic @Provides
        fun provideTrafficImageDialogFragment(trafficImage: TrafficImage): TrafficImageDialogFragment {
            return TrafficImageDialogFragment.newInstance(trafficImage)
        }
    }
}

