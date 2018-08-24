package com.gk.sgutil.bus.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.util.ArrayMap
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.gk.sgutil.R
import com.gk.sgutil.bus.model.BusDataManager
import com.gk.sgutil.bus.model.BusStop
import com.gk.sgutil.bus.viewmodel.BusRoutesResult
import com.gk.sgutil.bus.viewmodel.BusRoutesViewModel
import com.gk.sgutil.bus.viewmodel.BaseProgressViewModel.ProgressStatus
import com.gk.sgutil.util.AnimUtils
import com.gk.sgutil.util.Logger
import com.gk.sgutil.util.getErrorMessage
import kotlinx.android.synthetic.main.fragment_bus_routes_list.*
import kotlinx.android.synthetic.main.swipe_refresh_recyclerview.*
import javax.inject.Inject


class BusRoutesFragment : BaseFragment() {

    companion object {
        const val ARG_BUS_SERVICE = "bus_service_number"

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
        return inflater.inflate(R.layout.fragment_bus_routes_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    private fun updateUi(routeResult: BusRoutesResult?, direction: BusDirection) {
        updateStartEndBusStops(routeResult!!)
        // Update the list
        val route = if (direction == BusDirection.Direction1) routeResult.busRoutes1 else routeResult.busRoutes2
        recycler_view.swapAdapter(BusRoutesAdapter(context!!,
                if (route.isEmpty()) arrayOf() else route, routeResult.stopsMap, mBusDataManager), false)
        recycler_view.scheduleLayoutAnimation()

        // Update direction indicator
        if (direction != mDirection) {
            val rotation = AnimUtils.createRotateAnimation(
                    if (mDirection == BusDirection.Direction1) 0f else 180f,
                    if (mDirection == BusDirection.Direction1) 180f else 0f,
                    mSwitchDirectionAnimDuration)
            btn_change_direction.startAnimation(rotation)
            mDirection = direction
        }
        btn_change_direction.isEnabled = routeResult.busRoutes2.isNotEmpty()
    }

    // Update the start and end bus stop text
    private fun updateStartEndBusStops(routeResult: BusRoutesResult) {
        val route = routeResult.busRoutes1
        val stopMap = routeResult.stopsMap
        var stop = stopMap[route.first().busStopCode]
        txt_route_start.text = if (stop == null) getString(R.string.bus_stop_details_unknown) else stop.description
        stop = stopMap[route.last().busStopCode]
        txt_route_end.text = if (stop == null) getString(R.string.bus_stop_details_unknown) else stop.description
    }
}
