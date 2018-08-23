package com.gk.sgutil.bus.viewmodel

import com.gk.sgutil.bus.model.BusStop

/**
 * The interface for Activity to implement to jump among fragments
 */
interface BusActionController {

    /**
     * View bus arrivals information on selected bus stop
     */
    fun onViewBusArrivalsOnBusStop(busStop: BusStop)

    /**
     * View bus routes for the bus service
     */
    fun onViewBusRoutesForBusService(busServiceNo: String)
}