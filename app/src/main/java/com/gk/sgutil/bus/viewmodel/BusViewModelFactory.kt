package com.gk.sgutil.bus.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.gk.sgutil.util.Logger
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * ViewModelProvider.Factory implementation in order for Dagger injection.
 * Must use: ViewModelProviders.of(this, viewModelFactory).get(XXXViewModel::class.java)
 */
@Singleton
class BusViewModelFactory @Inject constructor(
        private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Logger.debug("Creating ViewModel for $modelClass")

        val creator = creators[modelClass] ?: creators.entries.firstOrNull {
            modelClass.isAssignableFrom(it.key)
        }?.value ?: throw IllegalArgumentException("unknown model class $modelClass")
        try {
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}
