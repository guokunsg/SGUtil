package com.gk.sgutil.bus.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.gk.sgutil.R
import com.gk.sgutil.bus.model.BusConfig
import com.gk.sgutil.bus.model.BusDataManager
import com.gk.sgutil.bus.model.BusRoute
import com.gk.sgutil.bus.model.BusStop
import com.gk.sgutil.bus.viewmodel.BaseProgressViewModel.ProgressStatus
import com.gk.sgutil.bus.viewmodel.BusRoutesResult
import com.gk.sgutil.bus.viewmodel.BusRoutesViewModel
import com.gk.sgutil.util.AnimUtils
import com.gk.sgutil.util.Logger
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_bus_routes.*
import kotlinx.android.synthetic.main.fragment_bus_routes_list.*
import kotlinx.android.synthetic.main.swipe_refresh_recyclerview.*
import javax.inject.Inject

class BusRoutesFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        const val ARG_BUS_SERVICE = "bus_service_number"

        // Default map initial zoom to show whole Singapore
        private const val MAP_INIT_ZOOM = 10.5f

        private const val TAB_INDEX_LIST = 0
        private const val TAB_INDEX_MAP = 1

        private enum class BusDirection {
            Direction1, Direction2
        }

        /**
         * Create the instance with the mandatory parameters
         * @param busService
         *      Bus service number
         */
        fun newInstance(busService: String): BusRoutesFragment {
            val fragment = BusRoutesFragment()
            val bundle = Bundle()
            bundle.putString(ARG_BUS_SERVICE, busService)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var mBusDataManager: BusDataManager

    // Bus service number
    private var mBusServiceNo: String? = null

    private lateinit var mModel: BusRoutesViewModel

    // GoogleMap will be set when the map is ready asynchronously.
    private var mMap: GoogleMap? = null
    // The UI view of google map
    private var mMapView: View? = null

    private var mTabIndex = 0

    // The current direction
    private var mDirection = BusDirection.Direction1

    // Duration for animation when switching bus direction
    private var mSwitchDirectionAnimDuration: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        // Get the bus stop
        mBusServiceNo = arguments!!.getString(ARG_BUS_SERVICE)
        if (mBusServiceNo == null) {
            throw RuntimeException(context.toString() + " expect bus service number argument")
        }

        mSwitchDirectionAnimDuration = context!!.resources.getInteger(R.integer.anim_switch_bus_direction_duration).toLong()
        createViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bus_routes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup maps
        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mMapView = view.findViewById(R.id.google_map)

        // Initialize RecyclerView
        with(recycler_view) {
            layoutManager = LinearLayoutManager(context)
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.recyclerview_falldown)
            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
            if (mModel.getBusRoutes().value == null) {
                adapter = BusRoutesAdapter(context!!, arrayOf(), ArrayMap<String, BusStop>(), mBusDataManager)
            }
        }
        swipe_refresh.setOnRefreshListener {
            findBusRoutes()
        }

        // Setup tab
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {}
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                mTabIndex = tab!!.position
                bus_routes_list.visibility = if (tab.position == TAB_INDEX_LIST) View.VISIBLE else View.GONE
                mMapView!!.visibility = if (tab.position == TAB_INDEX_MAP) View.VISIBLE else View.GONE
                updateUi(mModel.getBusRoutes().value, mDirection)
            }
        })
        tabs.getTabAt(mTabIndex)!!.select()

        // Update action bar title
        val act = activity!! as AppCompatActivity
        with(act.supportActionBar!!) {
            title = getString(R.string.bus_route_title, mBusServiceNo)
            subtitle = null
        }

        btn_change_direction.setOnClickListener {
            updateUi(mModel.getBusRoutes().value, revertDirection(mDirection))
        }

        findBusRoutes()
    }

    override fun onMapReady(map: GoogleMap?) {
        mMap = map
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(BusConfig.MAP_INIT_LOCATION, MAP_INIT_ZOOM))
        if (mTabIndex == TAB_INDEX_MAP) {
            updateUi(mModel.getBusRoutes().value, mDirection)
        }
    }

    // Create ViewModel and observe on the data
    private fun createViewModel() {
        mModel = ViewModelProviders.of(this, mViewModelFactory).get(BusRoutesViewModel::class.java)
        mModel.getBusRoutes().observe(this, Observer {
            updateUi(it!!, BusDirection.Direction1)
        })
        mModel.getProgress().observe(this, Observer {
            val progress = it!!
            Logger.debug("Received progress change. status=${progress.status} error=${progress.error}")
            swipe_refresh.isRefreshing = progress.status != ProgressStatus.Stopped
            if (progress.error != null)
                showError(progress.error)
        })
    }

    // Start a new search
    private fun findBusRoutes() {
        mModel.findBusRoutes(mBusServiceNo!!)
    }

    // Revert the current direction
    private fun revertDirection(currDirection: BusDirection): BusDirection {
        return if (currDirection == BusDirection.Direction1) BusDirection.Direction2 else BusDirection.Direction1
    }

    // Update UI to show the result
    private fun updateUi(routeResult: BusRoutesResult?, direction: BusDirection) {
        updateStartEndBusStops(routeResult)
        updateDirectionIndicator(routeResult, direction)
        val route = if (routeResult == null) null else
            if (direction == BusDirection.Direction1) routeResult.busRoutes1 else routeResult.busRoutes2
        updateList(routeResult, route)
        updateMap(routeResult, route)
    }

    // Update the start and end bus stop text
    private fun updateStartEndBusStops(routeResult: BusRoutesResult?) {
        if (routeResult == null) {
            txt_route_start.text = getString(R.string.bus_stop_details_na)
            txt_route_end.text = getString(R.string.bus_stop_details_na)
        } else {
            val route = routeResult.busRoutes1
            val stopMap = routeResult.stopsMap
            var stop = stopMap[route.first().busStopCode]
            txt_route_start.text = if (stop == null) getString(R.string.bus_stop_details_unknown) else stop.description
            stop = stopMap[route.last().busStopCode]
            txt_route_end.text = if (stop == null) getString(R.string.bus_stop_details_unknown) else stop.description
        }
    }

    // Update the bottom direction indicator
    private fun updateDirectionIndicator(routeResult: BusRoutesResult?, direction: BusDirection) {
        // Update direction indicator
        if (direction != mDirection) {
            val rotation = AnimUtils.createRotateAnimation(
                    if (mDirection == BusDirection.Direction1) 0f else 180f,
                    if (mDirection == BusDirection.Direction1) 180f else 0f,
                    mSwitchDirectionAnimDuration)
            btn_change_direction.startAnimation(rotation)
            mDirection = direction
        }
        btn_change_direction.isEnabled = routeResult != null && routeResult.busRoutes2.isNotEmpty()
    }

    // Update the list view
    private fun updateList(routeResult: BusRoutesResult?, route: Array<BusRoute>?) {
        if (mTabIndex != TAB_INDEX_LIST)
            return
        if (routeResult == null) {
            recycler_view.adapter = BusRoutesAdapter(context!!, arrayOf(), ArrayMap<String, BusStop>(), mBusDataManager)
        } else {
            recycler_view.swapAdapter(BusRoutesAdapter(context!!,
                    if (route!!.isEmpty()) arrayOf() else route, routeResult.stopsMap, mBusDataManager), false)
            recycler_view.scheduleLayoutAnimation()
        }
    }

    // Update the map view
    private fun updateMap(routeResult: BusRoutesResult?, route: Array<BusRoute>?) {
        if (mTabIndex != TAB_INDEX_MAP || mMap == null)
            return

        val map = mMap!!
        map.clear()
        if (routeResult == null || route == null)
            return

        // Add bus stop markers
        val stopMap = routeResult.stopsMap
        val boundBuilder = LatLngBounds.Builder()
        val markerMaker = MapMarkerMaker()
        val startColor = ContextCompat.getColor(context!!, R.color.route_start)
        val endColor = ContextCompat.getColor(context!!, R.color.route_end)
        for ((i, busRoute) in route.withIndex()) {
            val marker = MarkerOptions()
            val busStop = stopMap[busRoute.busStopCode]
            if (busStop != null) {
                // Create icon with the sequence number as the text
                val color = MapMarkerMaker.computeColor(startColor, endColor, route.size, i)
                val bitmap = markerMaker.createTextMarker(activity!!, busRoute.stopSequence.toString(), color)
                marker.position(LatLng(busStop.latitude, busStop.longitude))
                marker.title(busStop.busStopCode + " " + busStop.description)
                marker.icon(bitmap)
                map.addMarker(marker).tag = busStop

                // To build the minimum bound which shows all the markers
                boundBuilder.include(marker.position)
            }
        }

        // Map may not be loaded yet
        map.setOnMapLoadedCallback {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 0))
        }
    }
}
