package com.gk.sgutil.bus.viewmodel

import android.content.Context
import android.support.test.InstrumentationRegistry
import com.gk.sgutil.dagger.module.BusModelModule
import dagger.*
import dagger.android.AndroidInjector
import javax.inject.Singleton

/**
 *
 */
@Singleton
@Component(modules = [BusModelModule::class, TestModule::class])
interface TestComponent: AndroidInjector<Context> {
    fun inject(model: BusArrivalsViewModel)
    fun inject(model: BusRoutesViewModel)
}

@Module
abstract class TestModule {
    @Module
    companion object {
        @JvmStatic @Provides
        fun provideContext(): Context {
            return InstrumentationRegistry.getTargetContext()
        }
    }
}