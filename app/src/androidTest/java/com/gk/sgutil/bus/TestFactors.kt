package com.gk.sgutil.bus

import android.arch.persistence.room.Room
import android.content.Context
import com.gk.sgutil.bus.model.BusDatabase
import java.io.File

/**
 *
 */
const val ASSERT_BUS_STOP_SAMPLE = "bus_stops_sample.json"

const val ASSERT_BUS_ROUTE_SAMPLE = "bus_routes_sample.json"

fun createInMemoryBusDatabase(context: Context): BusDatabase {
    return Room.inMemoryDatabaseBuilder(context, BusDatabase::class.java).build()
}

fun clearData(context: Context) {
    // Delete shared preference
    val pref = context.getSharedPreferences("bus_config", Context.MODE_PRIVATE)
    val editor = pref.edit()
    for (key in pref.all.keys) {
        editor.remove(key)
    }
    editor.commit()
    val file = File(context.filesDir.parentFile, "shared_prefs/bus_config.xml")
    file.delete()
}