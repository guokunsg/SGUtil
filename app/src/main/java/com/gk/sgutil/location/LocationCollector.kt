package com.gk.sgutil.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.gk.sgutil.R
import com.gk.sgutil.SGUtilException
import com.gk.sgutil.util.Logger
import com.gk.sgutil.util.hasPermission
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

/**
 * Class to get the current location.
 * This implementation uses Google play location service.
 * Not thread safe!!
 */
class LocationCollector private constructor(
        context: Context, client: FusedLocationProviderClient, apiClientBuilder: GoogleApiClient.Builder) {

    private val mAppContext: Context = context.applicationContext
    private val mLocationClient: FusedLocationProviderClient = client
    // GoogleApiClient instances are not thread-safe. Create a GoogleApiClient on each thread.
    private val mApiClient: GoogleApiClient
    private var mLocationRequest: LocationRequest? = null

    private val mConnectionCallback = ConnectionCallback()
    private val mLocationCallback = LocalLocationCallback()

    // Android dependent functions which can be replaced for testing purpose
    private val fnCheckPermission = ::hasPermission
    private val fnGetLooper =  Looper::getMainLooper // Maybe should use other looper?

    private val mLocationSubject: BehaviorSubject<Location> = BehaviorSubject.create()

    companion object {
        /**
         * Create a new instance.
         */
        @JvmStatic
        fun newInstance(context: Context): LocationCollector {
            // non-Activity context behavior: Error resolutions will be automatically launched from the provided Context,
            // displaying system tray notifications when necessary.
            val appContext = context.applicationContext
            val locationClient = LocationServices.getFusedLocationProviderClient(appContext)
            return LocationCollector(appContext, locationClient, GoogleApiClient.Builder(appContext))
        }

        // For testing purposes
        @JvmStatic
        fun newInstanceForTesting(
                context: Context, locationClient: FusedLocationProviderClient,
                apiClientBuilder: GoogleApiClient.Builder): LocationCollector {
            return LocationCollector(context, locationClient, apiClientBuilder)
        }
    }

    init {
        mApiClient = apiClientBuilder
                .addConnectionCallbacks(mConnectionCallback)
                .addOnConnectionFailedListener(mConnectionCallback)
                .addApi(LocationServices.API)
                .build()
    }

    /**
     * Start to update locations
     * @return
     *      Location update event will be fired in the RX Subject
     */
    fun startLocationUpdate() : Subject<Location> {
        // Must have location permission
        if (! fnCheckPermission(mAppContext, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))) {
            mLocationSubject.onError(SGUtilException(SGUtilException.ErrorCode.NoLocationPermission))
            return mLocationSubject
        }

        if (mApiClient.isConnected) {
            requestLocationUpdates()
        } else {
            mApiClient.connect()
        }
        return mLocationSubject
    }

    /**
     * Stop location update
     */
    fun stopLocationUpdate() {
        mLocationClient.removeLocationUpdates(mLocationCallback)
        mApiClient.disconnect()
        Logger.debug("Location update stopped")
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val interval = mAppContext.resources.getInteger(R.integer.location_update_interval).toLong()
        // TODO: Seems location service doesn't really follow the interval?
        mLocationRequest = LocationRequest()
                .setInterval(interval)
                .setMaxWaitTime(interval)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        mLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, fnGetLooper())
                .addOnCompleteListener {
                    val err = it.exception
                    if (err != null) {
                        mLocationSubject.onError(SGUtilException(SGUtilException.ErrorCode.LocationServiceError, err))
                    }
                }

        Logger.debug("Location updates started")
        // Try to get the last location and update immediately
        getLastLocation()
    }

    /**
     * Read last location from LocationManager because
     * it seems fresher than FusedLocationProviderClient last location?
     */
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        val locationManager = mAppContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (networkLocation == null) {
            if (gpsLocation != null) {
                mLocationSubject.onNext(gpsLocation)
            }
        } else {
            if (gpsLocation == null) {
                mLocationSubject.onNext(networkLocation)
            } else {
                if (gpsLocation.elapsedRealtimeNanos > networkLocation.elapsedRealtimeNanos) {
                    mLocationSubject.onNext(gpsLocation)
                } else {
                    mLocationSubject.onNext(networkLocation)
                }
            }
        }
    }

    // Callback to receive GoogleApiClient connection result
    private inner class ConnectionCallback : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        override fun onConnected(connectionHint: Bundle?) {
            Logger.debug("Google play location service connected")
            requestLocationUpdates()
        }

        override fun onConnectionSuspended(cause : Int) {
            Logger.debug("Google play location service suspended. Cause: $cause")
        }

        override fun onConnectionFailed(result: ConnectionResult) {
            Logger.debug("Google play location service connection failed. Result: $result")
            mLocationSubject.onError(SGUtilException(SGUtilException.ErrorCode.LocationServiceError))
        }
    }

    // Callback to receive location update
    private inner class LocalLocationCallback : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation
            if (location != null) {
                Logger.debug("Location: $location")
                mLocationSubject.onNext(location)
            }
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            Logger.debug("onLocationAvailability: $availability")
        }
    }
}
