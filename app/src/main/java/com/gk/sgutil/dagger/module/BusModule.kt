package com.gk.sgutil.dagger.module

import dagger.Module

// Errors when using subcomponent.
// 1. There is a map to be injected into BusViewModelFactory constructor.
//    The map is created by Dagger, include module works but subcomponent will generate map not provided error
// 2. ViewModule needs to be in ActivityBindingModule mainActivity

@Module(includes = [BusModelModule::class, BusViewModelModule::class, LocationModule::class])
abstract class BusModule {
}