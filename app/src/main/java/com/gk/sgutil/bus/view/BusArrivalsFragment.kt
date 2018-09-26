package com.gk.sgutil.bus.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gk.sgutil.R
import com.gk.sgutil.bus.model.BusStop
import com.gk.sgutil.bus.viewmodel.BaseProgressViewModel.ProgressStatus
import com.gk.sgutil.bus.viewmodel.BusActionController
import com.gk.sgutil.bus.viewmodel.BusArrivals
import com.gk.sgutil.bus.viewmodel.BusArrivalsViewModel
import com.gk.sgutil.util.Logger
import com.google.gson.Gson
import kotlinx.android.synthetic.main.swipe_refresh_recyclerview.*

private const val ARG_BUS_STOP = "arg_bus_stop"

class BusArrivalsFragment : BaseFragment() {

    private lateinit var mBusStop: BusStop

    private lateinit var mModel: BusArrivalsViewModel

    private var mController: BusActionController? = null

    companion object {
        /**
         * Create the instance with the mandatory parameters
         * @param busStop
         *      The bus stop which to find arrival buses
         */
        fun newInstance(busStop: BusStop): BusArrivalsFragment {
            val fragment = BusArrivalsFragment()
            val bundle = Bundle()
            val gson = Gson()
            bundle.putString(ARG_BUS_STOP, gson.toJson(busStop))
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        // Get the bus stop
        val args = arguments
        if (args?.getString(ARG_BUS_STOP) == null) {
            throw RuntimeException(context.toString() + " expect bus stop argument")
        }
        val gson = Gson()
        mBusStop = gson.fromJson<BusStop>(args.getString(ARG_BUS_STOP), BusStop::class.java)

        createViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bus_arrivals_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        with(recycler_view) {
            layoutManager = LinearLayoutManager(context)
            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
            // Add empty adapter so that swipe refresh can work
            if (mModel.getBusArrivals().value == null) {
                adapter = BusArrivalsAdapter(context!!, arrayOf(), mController)
            }
        }
        swipe_refresh.setOnRefreshListener {
            findBusArrivals()
        }

        // Update action bar title
        val act = activity!! as AppCompatActivity
        with(act.supportActionBar!!) {
            title = mBusStop.description
            subtitle = getString(R.string.bus_stop_details_no_distance,
                    mBusStop.busStopCode, mBusStop.roadName)
        }
    }

    override fun onStart() {
        super.onStart()

        findBusArrivals()
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

    // Create ViewModel and observe on the data
    private fun createViewModel() {
        mModel = ViewModelProviders.of(this, mViewModelFactory).get(BusArrivalsViewModel::class.java)
        mModel.getBusArrivals()
                .observe(this, Observer {
                    updateUi(it)
                })
        mModel.getProgress()
                .observe(this, Observer {
                    val progress = it!!
                    Logger.debug("Received progress change. status=${progress.status} error=${progress.error}")
                    swipe_refresh.isRefreshing = progress.status != ProgressStatus.Stopped
                    if (progress.error != null)
                        showError(progress.error)
                })
    }

    // Start a new search
    private fun findBusArrivals() {
        mModel.findBusArrivals(mBusStop)
    }

    private fun updateUi(arrivals: BusArrivals?) {
        if (arrivals == null)
            return
        val services = arrivals.services
        Logger.debug("Updating adapter with ${services.size} services")
        recycler_view.swapAdapter(BusArrivalsAdapter(context!!, services, mController), false)
        recycler_view.scheduleLayoutAnimation()
    }
}
