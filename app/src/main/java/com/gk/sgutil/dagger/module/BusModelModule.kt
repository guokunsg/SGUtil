package com.gk.sgutil.dagger.module

import android.arch.persistence.room.Room
import android.content.Context
import com.gk.sgutil.bus.model.*
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 *
 */
@Module
abstract class BusModelModule() {

    @Module
    companion object {
        @JvmStatic @Singleton @Provides
        fun provideBusDatabase(context: Context): BusDatabase {
            return Room.databaseBuilder(
                    context.applicationContext, BusDatabase::class.java, DATABASE_NAME)
                    .build()
        }

        @JvmStatic @Singleton @Provides
        fun provideBusDataManager(context: Context, db: BusDatabase, dataService: BusDataService): BusDataManager {
            return BusDataManager(context, db, dataService)
        }

        @JvmStatic @Singleton @Provides
        fun provideBusDataService(): BusDataService {
            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit.create(BusDataService::class.java)
        }

        @JvmStatic @Singleton @Provides
        fun provideBusConfig(context: Context): BusConfig {
            return BusConfig(context)
        }
    }
}
