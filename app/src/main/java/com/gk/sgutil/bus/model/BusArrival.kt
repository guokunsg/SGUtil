package com.gk.sgutil.bus.model

import com.google.gson.annotations.SerializedName

/**
 * Data holder for bus arrival information.
 * Data format is defined in LTA doc section 2.1
 */
class BusArrival {

    companion object {
        const val BUS_LOAD_SEAT = "SEA"
        const val BUS_LOAD_STAND = "SDA"
        const val BUS_LOAD_LIMITED_STAND = "LSD"

        const val BUS_TYPE_SINGLE_DECK = "SD"
        const val BUS_TYPE_DOUBLE_DECK = "DD"
        const val BUS_TYPE_BENDY = "BD"
    }

    /**
     * Data holder for next bus
     */
    class NextBus {
        /**
         * Reference code of the first bus stop where this bus started its service
         */
        @SerializedName("OriginCode")
        var originCode: String? = null

        /**
         * Reference code of the last bus stop where this bus will terminate its service
         */
        @SerializedName("DestinationCode")
        var destinationCode: String? = null

        /**
         * Date-time of this bus’ estimated time of arrival, expressed in the UTC standard,
         * GMT+8 for Singapore Standard Time (SST)
         * This may be empty string and default Date cannot parse it
         */
        @SerializedName("EstimatedArrival")
        var estimatedArrival: String? = null

        /**
         * Current estimated location coordinates of this bus at point of published data
         */
        @SerializedName("Latitude")
        var latitude: String? = null

        /**
         * Current estimated location coordinates of this bus at point of published data
         */
        @SerializedName("Longitude")
        var longitude: String? = null

        /**
         * Ordinal value of the nth visit of this vehicle at this bus stop; 1=1st visit, 2=2nd visit
         */
        @SerializedName("VisitNumber")
        var visitNumber: String? = null

        /**
         * Current bus occupancy / crowding level:
         *      SEA (for Seats Available)
         *      SDA (for Standing Available)
         *      LSD (for Limited Standing)
         */
        @SerializedName("Load")
        var load: String? = null

        /**
         * Indicates if bus is wheel-chair accessible:
         *      WAB
         *      (empty / blank)
         */
        @SerializedName("Feature")
        var feature: String? = null

        /**
         * Vehicle type:
         *      SD (for Single Deck)
         *      DD (for Double Deck)
         *      BD (for Bendy)
         */
        @SerializedName("Type")
        var type: String? = null
    }

    /**
     * Represents the service block in the JSON data returned from the server
     */
    class BusService {
        @SerializedName("ServiceNo")
        var serviceNo : String? = null

        /**
         * Public Transport Operator Codes:
         *      SBST (for SBS Transit)
         *      SMRT (for SMRT Corporation)
         *      TTS (for Tower Transit Singapore)
         *      GAS (for Go Ahead Singapore)
         */
        @SerializedName("Operator")
        var operator : String? = null

        /**
         * Next bus information
         */
        @SerializedName("NextBus")
        var nextBus: NextBus? = null

        /**
         * May be empty if there is no second bus
         */
        @SerializedName("NextBus2")
        var nextBus2 : NextBus? = null

        /**
         * May be empty if there is no thrid bus
         */
        @SerializedName("NextBus3")
        var nextBus3 : NextBus? = null
    }

    @SerializedName("odata.metadata")
    var metadata: String? = null

    @SerializedName("BusStopCode")
    var busStopCode : String? = null

    /**
     * Array of all bus services currently available on the stop
     */
    @SerializedName("Services")
    var services: Array<BusService>? = null
}