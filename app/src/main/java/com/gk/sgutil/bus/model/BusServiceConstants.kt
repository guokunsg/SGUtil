package com.gk.sgutil.bus.model

/**
 * Base Url for the LTA bus services
 */
const val BASE_URL = "http://datamall2.mytransport.sg/"

/**
 * Endpoint to get the bus stops.
 */
const val BUS_STOPS_ENDPOINT = "ltaodataservice/BusStops"

/**
 * Endpoint to get the bus routes.
 */
const val BUS_ROUTES_ENDPOINT = "ltaodataservice/BusRoutes"

/**
 * Endpoint to query the bus arrival information
 */
const val BUS_ARRIVAL_ENDPOINT = "ltaodataservice/BusArrivalv2"

const val TRAFFIC_IMAGES_ENDPOINT = "ltaodataservice/Traffic-Images"

/**
 * Account key to be set in the header in order to make a successful call
 */
const val HEADER_ACCOUNT_KEY = "AccountKey: fH270ARxS1SpB9rwJGYt2w=="