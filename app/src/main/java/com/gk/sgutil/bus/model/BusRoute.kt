package com.gk.sgutil.bus.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.support.annotation.NonNull
import com.google.gson.annotations.SerializedName

/**
 * 1. Data holder for a single bus route in JSON data,
 *    format is defined in LTA doc section 2.3 Bus routes
 * 2. Database entity class for ROOM
 * May not be a good design to combine external and internal data, just for test trial
 */
@Entity(tableName = TABLE_BUS_ROUTES,
        primaryKeys = arrayOf(
                BusRoutesColumn.SERVICE_NO,
                BusRoutesColumn.DIRECTION,
                BusRoutesColumn.STOP_SEQUENCE))
class BusRoute {
    class BusRoutes {
        @SerializedName("odata.metadata")
        val metadata: String? = null

        @SerializedName("value")
        val routes: Array<BusRoute>? = null
    }

    @SerializedName("ServiceNo")
    @ColumnInfo(name = BusRoutesColumn.SERVICE_NO)
    @NonNull
    var serviceNo: String? = null

    @SerializedName("Operator")
    @ColumnInfo(name = BusRoutesColumn.OPERATOR)
    var operator: String? = null

    @SerializedName("Direction")
    @ColumnInfo(name = BusRoutesColumn.DIRECTION, typeAffinity = ColumnInfo.INTEGER)
    var direction: Int = 0

    @SerializedName("StopSequence")
    @ColumnInfo(name = BusRoutesColumn.STOP_SEQUENCE, typeAffinity = ColumnInfo.INTEGER)
    var stopSequence: Int = 0

    @SerializedName("BusStopCode")
    @ColumnInfo(name = BusRoutesColumn.BUS_STOP_CODE)
    @NonNull
    var busStopCode: String? = null

    @SerializedName("Distance")
    @ColumnInfo(name = BusRoutesColumn.DISTANCE, typeAffinity = ColumnInfo.REAL)
    var distance: Double = 0.0

    @SerializedName("WD_FirstBus")
    @ColumnInfo(name = BusRoutesColumn.WD_FIRST_BUS)
    var wd_FirstBus: String? = null

    @SerializedName("WD_LastBus")
    @ColumnInfo(name = BusRoutesColumn.WD_LAST_BUS)
    var wd_LastBus: String? = null

    @SerializedName("SAT_FirstBus")
    @ColumnInfo(name = BusRoutesColumn.SAT_FIRST_BUS)
    var sat_FirstBus: String? = null

    @SerializedName("SAT_LastBus")
    @ColumnInfo(name = BusRoutesColumn.SAT_LAST_BUS)
    var sat_LastBus: String? = null

    @SerializedName("SUN_FirstBus")
    @ColumnInfo(name = BusRoutesColumn.SUN_FIRST_BUS)
    var sun_FirstBus: String? = null

    @SerializedName("SUN_LastBus")
    @ColumnInfo(name = BusRoutesColumn.SUN_LAST_BUS)
    var sun_LastBus: String? = null

    @ColumnInfo(name = BusRoutesColumn.SYNC_TIME)
    var syncTime: Long = 0

    override fun toString(): String {
        return "BusRoute: service=$serviceNo direction=$direction stopSequence=$stopSequence stopCode=$busStopCode " +
                "operator=$operator distance=$distance"
    }
}
