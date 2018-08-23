package com.gk.sgutil.bus.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gk.sgutil.R
import com.gk.sgutil.bus.viewmodel.BusActionController
import com.gk.sgutil.bus.viewmodel.BusStopNearbyInfo
import kotlinx.android.synthetic.main.fragment_bus_stops_list_item.view.*

class BusStopsNearbyAdapter(
        private val mValues: Array<BusStopNearbyInfo>,
        private val mController: BusActionController?)
    : RecyclerView.Adapter<BusStopsNearbyAdapter.ViewHolder>() {

    private val mOnClickListener = View.OnClickListener { v ->
        val busStop = v.tag as BusStopNearbyInfo
        mController!!.onViewBusArrivalsOnBusStop(busStop.busStop)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_bus_stops_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mNameView.text = item.busStop.description
        holder.mDetailsView.text = holder.mNameView.context.getString(R.string.bus_stop_details,
                item.busStop.busStopCode, item.busStop.roadName, item.distance.toInt())

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameView: TextView = mView.bus_stop_name
        val mDetailsView: TextView = mView.details
    }
}
