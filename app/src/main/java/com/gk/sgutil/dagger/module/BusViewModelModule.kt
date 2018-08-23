package com.gk.sgutil.dagger.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.gk.sgutil.bus.viewmodel.*
import com.gk.sgutil.dagger.scope.ActivityScoped
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Provider
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

/**
 *
 */
@Module
abstract class BusViewModelModule {

    @Binds @IntoMap
    @ViewModelKey(BusStopNearbyViewModel::class)
    abstract fun bindBusStopNearbyViewModel(viewModel: BusStopNearbyViewModel): ViewModel

    @Binds @IntoMap
    @ViewModelKey(BusArrivalsViewModel::class)
    abstract fun bindBusArrivalsViewModel(viewModel: BusArrivalsViewModel): ViewModel

    @Binds @IntoMap
    @ViewModelKey(BusRoutesViewModel::class)
    abstract fun bindBusRoutesViewModel(viewModel: BusRoutesViewModel): ViewModel

    @Binds @IntoMap
    @ViewModelKey(TrafficImagesViewModel::class)
    abstract fun bindTrafficImagesViewModel(viewModel: TrafficImagesViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: BusViewModelFactory): ViewModelProvider.Factory
}


