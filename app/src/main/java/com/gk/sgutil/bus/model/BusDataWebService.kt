package com.gk.sgutil.bus.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * LTA bus data web services
 */
interface BusDataService {
    /**
     * Make the call to get the bus stops.
     * Each call returns 500 records (server controlled value) at most due to the website limitation.
     * Set skip to get next block of records.
     */
    @GET(BUS_STOPS_ENDPOINT)
    @Headers(HEADER_ACCOUNT_KEY)
    fun getBusStops(@Query("\$skip") skip: Int?): Call<BusStop.BusStops>

    /**
     * Make the call to get the bus routes.
     * Each call returns 500 records (server controlled value) at most due to the website limitation.
     * Set skip to get next block of records.
     */
    @GET(BUS_ROUTES_ENDPOINT)
    @Headers(HEADER_ACCOUNT_KEY)
    fun getBusRoutes(@Query("\$skip") skip: Int?): Call<BusRoute.BusRoutes>

    /**
     * Query for bus arrival information.
     * @param busStopCode
     *      Bus stop code to get bus arrivals on this stop.
     */
    @GET(BUS_ARRIVAL_ENDPOINT)
    @Headers(HEADER_ACCOUNT_KEY)
    fun getBusArrival(@Query("BusStopCode") busStopCode: String): Call<BusArrival>

    @GET(TRAFFIC_IMAGES_ENDPOINT)
    @Headers(HEADER_ACCOUNT_KEY)
    fun getTrafficImages(): Call<TrafficImage.TrafficImages>
}

