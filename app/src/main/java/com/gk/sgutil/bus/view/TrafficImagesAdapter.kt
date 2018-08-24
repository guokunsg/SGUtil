package com.gk.sgutil.bus.view

import android.content.Context
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gk.sgutil.R
import com.gk.sgutil.bus.model.TrafficImage
import com.gk.sgutil.location.AddressFinder
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_traffic_images_item.view.*

class TrafficImagesAdapter(
        private val mContext: Context,
        private val mValues: Array<TrafficImage>,
        private val mAddressFinder: AddressFinder,
        private val mActionListener: ActionListener)
    : RecyclerView.Adapter<TrafficImagesAdapter.ViewHolder>() {

    // The map from the location geocode to address
    private val mAddressMap = ArrayMap<LatLng, String>()
    private val mLayoutInflater = LayoutInflater.from(mContext)

    interface ActionListener {
        fun onImageClicked(image: TrafficImage)
    }

    private val mOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        val image = v.tag as TrafficImage
        mActionListener.onImageClicked(image)
    }

    init {
        // Initialize with the cached addresses
        for (image in mValues) {
            val location = LatLng(image.latitude, image.longitude)
            val address = mAddressFinder.getAddress(location)
            if (address != null) {
                mAddressMap.put(location, address.thoroughfare)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mLayoutInflater.inflate(R.layout.fragment_traffic_images_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        val location = LatLng(item.latitude, item.longitude)
        holder.address.text = findAddress(location)
        holder.image.setImageURI(item.imageLink)
        holder.image.tag = item
        holder.image.setOnClickListener(mOnClickListener)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val image = mView.image!!
        val address = mView.txt_address!!
    }

    private fun findAddress(location: LatLng): String {
        var address = mAddressMap[location]
        if (address != null)
            return address
        // Default display if the address is not found
        address = AddressFinder.formatAddress(location, null)
        // Set an address first to prevent loop
        mAddressMap[location] = address
        // Start a finding and update UI later
        mAddressFinder.findAddresses(arrayOf(location))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mAddressMap[location] = AddressFinder.formatAddress(location, it[0])
                    notifyDataSetChanged()
                }
        return address
    }
}
