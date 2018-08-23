package com.gk.sgutil.bus.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import com.google.gson.annotations.SerializedName

/**
 * 1. Data holder for a single bus stop in JSON data,
 *    format is defined in LTA doc section 2.4 Bus stops.
 * 2. Database entity class for ROOM
 */
@Entity(tableName = TABLE_BUS_STOPS)
class BusStop {
    /**
     * Represents the JSON data returned from the server.
     */
    class BusStops {
        @SerializedName("odata.metadata")
        var metadata: String? = null

        @SerializedName("value")
        var busStops: Array<BusStop>? = null
    }

    /**
     * The unique 5-digit identifier for this physical bus stop
     */
    @SerializedName("BusStopCode")
    @PrimaryKey @NonNull
    @ColumnInfo(name = BusStopsColumn.BUS_STOP_CODE)
    var busStopCode: String? = null

    /**
     * The road on which this bus stop is located
     */
    @SerializedName("RoadName")
    @ColumnInfo(name = BusStopsColumn.ROAD_NAME)
    var roadName: String? = null

    /**
     * Landmarks next to the bus stop (if any) to aid in identifying this bus stop
     */
    @SerializedName("Description")
    @ColumnInfo(name = BusStopsColumn.DESCRIPTION)
    var description: String? = null

    @SerializedName("Latitude")
    @ColumnInfo(name = BusStopsColumn.LATITUDE, typeAffinity = ColumnInfo.REAL)
    var latitude: Double = 0.0

    @SerializedName("Longitude")
    @ColumnInfo(name= BusStopsColumn.LONGITUDE, typeAffinity = ColumnInfo.REAL)
    var longitude: Double = 0.0

    @ColumnInfo(name= BusStopsColumn.SYNC_TIME, typeAffinity = ColumnInfo.INTEGER)
    var syncTime: Long = 0
}
