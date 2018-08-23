package com.gk.sgutil.bus.view

import android.content.Context
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.gk.sgutil.R
import com.gk.sgutil.bus.model.BusDataManager
import com.gk.sgutil.bus.model.BusRoute
import com.gk.sgutil.bus.model.BusStop
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_bus_routes_list_item.view.*

/**
 *
 */
class BusRoutesAdapter(
        private val mContext: Context,
        private val mValues: Array<BusRoute>,
        private val mMap: Map<String, BusStop>,
        private val mBusDataManager: BusDataManager)
    : RecyclerView.Adapter<BusRoutesAdapter.ViewHolder>() {

    // A map from bus stop code to an array of bus services on this stop
    private val mBusServicesMap = ArrayMap<String, Array<String>>()
    private var mExpandedPosition = -1

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_bus_routes_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val route = mValues[position]

        holder.mView.tag = route
        holder.updateMainInfo(route)
        // Setup extra information depending the expansion status
        val isExpanded = position == mExpandedPosition
        holder.showExtraInfo(route, isExpanded)
        holder.itemView.isActivated = isExpanded
        holder.itemView.setOnClickListener {
            mExpandedPosition = if (isExpanded) -1 else position
            if (mBusServicesMap[route.busStopCode!!] == null)
                loadBusStops(route.busStopCode!!)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val sequence = mView.sequence!!
        val stopName = mView.bus_stop!!
        val details = mView.details!!

        val extra = mView.extra_info!!
        val oper_time = mView.oper_time!!
        val buses = mView.buses!!

        // Update the information which are always displayed
        fun updateMainInfo(route: BusRoute) {
            mView.tag = route
            sequence.text = route.stopSequence.toString()
            val busStop = mMap.get(route.busStopCode)
            if (busStop != null) {
                stopName.text = busStop.description
                details.text = mContext.getString(
                        R.string.bus_stop_details_no_distance, busStop.busStopCode, busStop.roadName)
            } else {
                stopName.text = mContext.getString(R.string.bus_stop_details_unknown)
                details.text = mContext.getString(R.string.bus_stop_details_unknown)
            }
        }

        // Show/hide extra information
        fun showExtraInfo(route: BusRoute, isExpanded: Boolean) {
            if (isExpanded && extra.visibility != View.VISIBLE) {
                extra.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bus_route_show_details))
            }
            extra.visibility = if (isExpanded) View.VISIBLE else View.GONE
            if (isExpanded) {
                // Setup extra information
                oper_time.text = mContext.getString(R.string.bus_route_oper_time,
                        route.wd_FirstBus, route.wd_LastBus, route.sat_FirstBus, route.sat_LastBus,
                        route.sun_FirstBus, route.sun_LastBus, route.distance.toString())
                val buses = mBusServicesMap[route.busStopCode]
                if (buses != null && buses.isNotEmpty()) {
                    val sb = StringBuilder()
                    for (bus in buses)
                        sb.append(bus).append(" ")
                    this.buses.text = mContext.getString(R.string.bus_route_buses, sb.toString())
                    this.buses.visibility = View.VISIBLE
                } else {
                    this.buses.visibility = View.GONE
                }
            } else {
                this.oper_time.text = null
                this.buses.visibility = View.GONE
            }
        }
    }

    /**
     * Load bus stops in the background thread and update adapter after finished
     */
    private fun loadBusStops(busStopCode: String) {
        Observable.create<Array<String>> {
            val busServices = mBusDataManager.getBusRouteDao().loadBusServices(busStopCode)
            it.onNext(busServices)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    // Put the data into the map and trigger an update so it will be reflected
                    mBusServicesMap[busStopCode] = it
                    notifyDataSetChanged()
                }
    }
}
