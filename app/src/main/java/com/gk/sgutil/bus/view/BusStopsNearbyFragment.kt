package com.gk.sgutil.bus.view

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gk.sgutil.R
import com.gk.sgutil.SGUtilException
import com.gk.sgutil.bus.model.BusConfig
import com.gk.sgutil.bus.viewmodel.BusActionController
import com.gk.sgutil.bus.viewmodel.BusStopNearbyInfo
import com.gk.sgutil.bus.viewmodel.BusStopNearbyViewModel
import com.gk.sgutil.bus.viewmodel.BusStopsNearby
import com.gk.sgutil.util.Logger
import com.gk.sgutil.util.getErrorMessage
import com.gk.sgutil.util.hasPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_bus_stops.*
import kotlinx.android.synthetic.main.swipe_refresh_recyclerview.*
import javax.inject.Inject

class BusStopsNearbyFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private const val TAB_INDEX_LIST = 0
        private const val TAB_INDEX_MAP = 1

        // Move the camera to Singapore when the map is ready
        private val MAP_INIT_LOCATION = LatLng(1.327, 103.826)
        // Show whole Singapore
        private const val MAP_INIT_ZOOM = 10.5f
        // Show street
        private const val MAP_DEFAULT_ZOOM = 17.5f

        private const val MOVE_CAMERA_ANIM_DURATION = 1000
        private const val OLD_LOCATION_TIME = 180000
    }

    private lateinit var mModel: BusStopNearbyViewModel

    // GoogleMap will be set when the map is ready asynchronously.
    private var mMap: GoogleMap? = null
    private var mMapView: View? = null

    // Save the selected tab index and restore the selection when new view is created
    // Not using saveInstanceState because retainInstance
    private var mTabIndex = TAB_INDEX_LIST

    // Whether should move the camera when there is a location change.
    // Camera moves when first and user refresh triggered location change
    private var mMoveCamera = true
    private var mLastMapLocation : LatLng? = null

    private var mController: BusActionController? = null
    @Inject
    lateinit var mBusConfig: BusConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        AndroidSupportInjection.inject(this)
        createViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bus_stops, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup maps
        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mMapView = view.findViewById(R.id.google_map)

        // Setup list view
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        // Add empty adapter so that swipe refresh can work
        if (mModel.getBusStopsNearby().value == null) {
            recycler_view.adapter = BusStopsNearbyAdapter(arrayOf(), mController)
        }

        swipe_refresh.setOnRefreshListener {
            mMoveCamera = true
            grantPermssionAndStart()
            swipe_refresh.isRefreshing = false
        }

        // Setup tab
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {}
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                mTabIndex = tab!!.position
                mBusConfig.setBusStopsNearbyLastViewedTab(mTabIndex)
                Logger.debug("Tab selected: $mTabIndex")
                swipe_refresh.visibility = if (tab.position == TAB_INDEX_LIST) View.VISIBLE else View.GONE
                mMapView!!.visibility = if (tab.position == TAB_INDEX_MAP) View.VISIBLE else View.GONE
                mMoveCamera = true
                updateUi(mModel.getBusStopsNearby().value)
            }
        })
        mTabIndex = mBusConfig.getBusStopsNearbyLastViewedTab()
        tabs.getTabAt(mTabIndex)!!.select()

        // Update action bar title
        val act = activity!! as AppCompatActivity
        with(act.supportActionBar!!) {
            title = getString(R.string.bus_stops_nearby_title)
            subtitle = null
        }
    }

    override fun onStart() {
        super.onStart()
        mMoveCamera = true
        mLastMapLocation = null
        grantPermssionAndStart()
    }

    override fun onStop() {
        super.onStop()
        stopSearch()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BusActionController) {
            mController = context
        } else {
            throw RuntimeException(context.toString() + " must implement BusActionController")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mController = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMap = null
    }

    override fun onMapReady(map: GoogleMap?) {
        mMap = map
        if (mModel.getBusStopsNearby().value == null)
            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_INIT_LOCATION, MAP_INIT_ZOOM))
    }

    // Create ViewModel and observe on the data
    private fun createViewModel() {
        // Dagger inject with the factory
        mModel = ViewModelProviders.of(this, mViewModelFactory).get(BusStopNearbyViewModel::class.java)
        mModel.getBusStopsNearby().observe(this, Observer { result -> updateUi(result) })
        mModel.getError().observe(this, Observer { error -> showError(error!!) })
    }

    // Check and grant location permission. Start searching after the permission is granted
    @SuppressLint("MissingPermission")
    private fun grantPermssionAndStart() {
        RxPermissions(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe {
                    if (it) {
                        startSearch()
                    } else {
                        showError(SGUtilException(SGUtilException.ErrorCode.NoLocationPermission))
                    }
                }
    }

    private fun startSearch() {
        swipe_refresh.isRefreshing = true
        mModel.startLocationCollection()
    }

    private fun stopSearch() {
        swipe_refresh.isRefreshing = false
        mModel.stopLocationCollection()
    }

    // Display the error to the user and handle the action when the user accepts it
    override fun showError(error: Throwable) {
        swipe_refresh.isRefreshing = false
        val noPermission = if (error is SGUtilException)
            error.errCode == SGUtilException.ErrorCode.NoLocationPermission
        else false
        Snackbar.make(recycler_view, getErrorMessage(error, context!!),
                if (noPermission) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG)
                .setAction(android.R.string.ok) {
                    // Handle the error by the type
                    if (noPermission) {
                        grantPermssionAndStart()
                    }
                }
                .show()
    }

    // Trigger an UI update if there is already data available
    private fun updateUi(busStops: BusStopsNearby?) {
        swipe_refresh.isRefreshing = false
        updateList(busStops)
        updateMap(busStops)
    }

    // Update list to show the latest bus stops nearby
    private fun updateList(stopsNearby: BusStopsNearby?) {
        if (tabs.selectedTabPosition == TAB_INDEX_MAP)
            return
        val stops = if (stopsNearby == null) arrayOf() else stopsNearby.busStops
        recycler_view.swapAdapter(BusStopsNearbyAdapter(stops, mController), false)
        recycler_view.scheduleLayoutAnimation()
    }

    // Update GoogleMap to show the latest bus stops nearby
    @SuppressLint("MissingPermission")
    private fun updateMap(stops: BusStopsNearby?) {
        if (mMap == null || tabs.selectedTabPosition == TAB_INDEX_LIST)
            return

        val map = mMap!!
        map.clear()
        if (stops == null)
            return

        val location = stops.location
        val loc = LatLng(location.latitude, location.longitude)
        // Move camera for first time and user triggered actions
        if (mMoveCamera) {
            // The location might be an old location. Ignore the same location
            if (mLastMapLocation == null || ! loc.equals(mLastMapLocation)) {
                val cameraPos = CameraPosition(loc, MAP_DEFAULT_ZOOM,
                        map.cameraPosition.tilt, map.cameraPosition.bearing)
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos), MOVE_CAMERA_ANIM_DURATION, null)
                // Only consider camera moved for newer locations
                if (System.currentTimeMillis() - location.time < OLD_LOCATION_TIME) {
                    mMoveCamera = false
                }
            }
        }
        // Show blue my location on the map
        if (hasPermission(context!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)))
            map.isMyLocationEnabled = true
        // Add bus stop markers
        val icon = BitmapDescriptorFactory.fromResource(R.mipmap.bus_marker)
        for (busStop in stops.busStops) {
            val marker = MarkerOptions()
            marker.position(LatLng(busStop.busStop.latitude, busStop.busStop.longitude))
            marker.title(busStop.busStop.busStopCode + " " + busStop.busStop.description)
            marker.icon(icon)
            map.addMarker(marker).tag = busStop
        }
        // Show buses arriving when user selects a bus stop
        map.setOnInfoWindowClickListener { marker ->
            val busStopInfo = marker.tag as BusStopNearbyInfo
            mController!!.onViewBusArrivalsOnBusStop(busStopInfo.busStop)
        }
        mLastMapLocation = loc
    }
}
