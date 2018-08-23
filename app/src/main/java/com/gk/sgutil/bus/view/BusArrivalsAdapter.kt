package com.gk.sgutil.bus.view

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.gk.sgutil.R
import com.gk.sgutil.bus.model.BusArrival
import com.gk.sgutil.bus.viewmodel.BusActionController
import kotlinx.android.synthetic.main.fragment_bus_arrivals_list_item.view.*
import kotlinx.android.synthetic.main.fragment_bus_arrivals_list_item_next_bus.view.*
import java.text.SimpleDateFormat
import java.util.*

class BusArrivalsAdapter(
        private val mContext: Context,
        private val mValues: Array<BusArrival.BusService>,
        private val mController: BusActionController?)
    : RecyclerView.Adapter<BusArrivalsAdapter.ViewHolder>() {

    // Different colors for bus load
    private val COLOR_SEAT = ContextCompat.getColor(mContext, R.color.bus_load_seat)
    private val COLOR_STAND = ContextCompat.getColor(mContext, R.color.bus_load_stand)
    private val COLOR_LIMITED = ContextCompat.getColor(mContext, R.color.bus_load_limited)

    private val TEXT_ARRIVING = mContext.getString(R.string.bus_arriving)
    private val TEXT_NA = mContext.getString(R.string.bus_not_available)

    // JSON ISO8601 date format parser
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)

    private val mOnClickListener = View.OnClickListener { v ->
        val busService = v.tag as BusArrival.BusService
        mController!!.onViewBusRoutesForBusService(busService.serviceNo!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_bus_arrivals_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mServiceNo.text = item.serviceNo
        holder.mNextBus.bind(item.nextBus)
        holder.mNextBus2.bind(item.nextBus2)
        holder.mServiceNo.tag = item
        holder.mServiceNo.setOnClickListener(mOnClickListener)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        holder.mNextBus.highLightArriving()
    }

    inner class NextBusHolder(val arrTime: TextView,
                              val minutes: TextView,
                              val bus_load: View,
                              val bus_type: ImageView) {

        fun bind(nextBus: BusArrival.NextBus?) {
            bindArrTime(nextBus)
            bindLoad(nextBus)
            bindBusType(nextBus)
        }

        private fun bindArrTime(nextBus: BusArrival.NextBus?) {
            minutes.visibility = View.GONE
            if (nextBus == null || TextUtils.isEmpty(nextBus.estimatedArrival)) {
                arrTime.text = TEXT_NA
                TextViewCompat.setTextAppearance(arrTime, R.style.BusArrival_NotAvailable)
                return
            }
            val time = dateFormat.parse(nextBus.estimatedArrival)
            val mins = (time.time - System.currentTimeMillis()) / 60000
            if (mins < 1L) {
                arrTime.text = TEXT_ARRIVING
                TextViewCompat.setTextAppearance(arrTime, R.style.BusArrival_Arriving)
            } else {
                arrTime.text = mins.toString()
                TextViewCompat.setTextAppearance(arrTime, R.style.BusArrival_InMins)
                minutes.visibility = View.VISIBLE
            }
        }

        private fun bindLoad(nextBus: BusArrival.NextBus?) {
            if (nextBus == null) {
                bus_load.visibility = View.INVISIBLE
                return
            }
            when (nextBus.load) {
                BusArrival.BUS_LOAD_SEAT -> {
                    bus_load.setBackgroundColor(COLOR_SEAT);
                    bus_load.visibility = View.VISIBLE
                }
                BusArrival.BUS_LOAD_STAND -> {
                    bus_load.setBackgroundColor(COLOR_STAND);
                    bus_load.visibility = View.VISIBLE
                }
                BusArrival.BUS_LOAD_LIMITED_STAND -> {
                    bus_load.setBackgroundColor(COLOR_LIMITED);
                    bus_load.visibility = View.VISIBLE
                }
                else -> bus_load.visibility = View.INVISIBLE
            }
        }

        private fun bindBusType(nextBus: BusArrival.NextBus?) {
            if (nextBus == null) {
                bus_type.visibility = View.INVISIBLE
                return
            }
            when(nextBus.type) {
                BusArrival.BUS_TYPE_SINGLE_DECK -> {
                    bus_type.setImageResource(R.mipmap.single_deck)
                    bus_type.visibility = View.VISIBLE
                }
                BusArrival.BUS_TYPE_DOUBLE_DECK -> {
                    bus_type.setImageResource(R.mipmap.double_deck)
                    bus_type.visibility = View.VISIBLE
                }
                BusArrival.BUS_TYPE_BENDY -> {
                    bus_type.setImageResource(R.mipmap.bendy)
                    bus_type.visibility = View.VISIBLE
                }
                else -> bus_type.visibility = View.GONE
            }
        }

        fun highLightArriving() {
            if (TEXT_ARRIVING.equals(arrTime.text)) {
                val anim = AnimationUtils.loadAnimation(mContext, R.anim.bus_arriving)
                arrTime.startAnimation(anim)
            } else {
                arrTime.clearAnimation()
            }
        }
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mServiceNo = mView.service_no!!
        val mNextBus = NextBusHolder(mView.next_bus.arrive_time,
                mView.next_bus.minutes, mView.next_bus.bus_load, mView.next_bus.bus_type)
        val mNextBus2 = NextBusHolder(mView.next_bus2.arrive_time,
                mView.next_bus2.minutes, mView.next_bus2.bus_load, mView.next_bus2.bus_type)
    }
}
