package com.gk.sgutil.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.support.v4.util.LruCache
import com.gk.sgutil.util.Logger
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import java.util.*

/**
 * Find address with the location.
 * Call findAddresses to request a search for the input addresses. It may involve network search and may take some time.
 * The results will be cached with LRU strategy
 */
class AddressFinder(context: Context) {
    private val mGeocoder = Geocoder(context.applicationContext, Locale.getDefault())
    private val mCache = LruCache<LatLng, Address>(300)

    companion object {
        /**
         * Format the location address as text.
         * If address is not null, build text from the address.
         * If address is null, return text with location latitude and longitude.
         */
        @JvmStatic
        fun formatAddress(location: LatLng, address: Address?): String {
            if (address != null) {
                val text = address.thoroughfare
                if (text != null && text.isNotEmpty())
                    return text
            }
            return "(${location.latitude}, ${location.longitude})"
        }
    }

    /**
     * Start a search for the input addresses.
     * If there is a matched local cache, the cached result will be used.
     * Otherwise, network search may be involved.
     * If no address is found, the object in the corresponding position is null
     */
    fun findAddresses(locations: Array<LatLng>): Observable<Array<Address?>> {
        return Observable.create {
            val list = ArrayList<Address?>()
            for (location in locations) {
                // Get from cache. If there is no cached one, use Geocoder to find. May take a while
                var address = getAddress(location)
                if (address == null) {
                    val addresses = mGeocoder.getFromLocation(
                            location.latitude, location.longitude, 1)
                    for (add in addresses)
                        Logger.debug("$add")
                    if (addresses.size > 0) {
                        address = addresses[0]
                        setAddress(location, address)
                    }
                }
                list.add(address) // Address may be null
            }
            it.onNext(list.toTypedArray())
            it.onComplete()
        }
    }

    /**
     * Get the address for the input location.
     * Only return the result in the memory cache.
     */
    fun getAddress(location: LatLng): Address? {
        synchronized(mCache) {
            return mCache.get(location)
        }
    }

    private fun setAddress(location: LatLng, address: Address) {
        synchronized(mCache) {
            mCache.put(location, address)
        }
    }
}