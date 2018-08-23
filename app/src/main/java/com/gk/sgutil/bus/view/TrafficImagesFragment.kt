package com.gk.sgutil.bus.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gk.sgutil.R
import com.gk.sgutil.bus.model.TrafficImage
import com.gk.sgutil.bus.viewmodel.BaseProgressViewModel
import com.gk.sgutil.bus.viewmodel.TrafficImagesViewModel
import com.gk.sgutil.location.AddressFinder
import com.gk.sgutil.util.Logger
import kotlinx.android.synthetic.main.swipe_refresh_recyclerview.*
import javax.inject.Inject

class TrafficImagesFragment: BaseFragment() {

    private lateinit var mModel: TrafficImagesViewModel
    @Inject
    lateinit var mAddressFinder: AddressFinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        createViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_traffic_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update action bar title
        val act = activity!! as AppCompatActivity
        with(act.supportActionBar!!) {
            title = getString(R.string.traffic_images)
        }

        swipe_refresh.setOnRefreshListener {
            mModel.refreshImages()
        }

        with(recycler_view) {
            layoutManager = GridLayoutManager(context, 2)
            val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }
    }

    override fun onStart() {
        super.onStart()

        mModel.refreshImages()
    }

    private fun createViewModel() {
        mModel = ViewModelProviders.of(this, mViewModelFactory).get(TrafficImagesViewModel::class.java)
        mModel.getTrafficImages()
                .observe(this, Observer {
                    recycler_view.swapAdapter(
                            TrafficImagesAdapter(context!!, it!!, mAddressFinder, mActionListener), false)
                })
        mModel.getProgress()
                .observe(this, Observer {
                    val progress = it!!
                    Logger.debug("Received progress change. status=${progress.status} error=${progress.error}")
                    swipe_refresh.isRefreshing = progress.status != BaseProgressViewModel.ProgressStatus.Stopped
                    if (progress.error != null)
                        showError(progress.error)
                })
    }

    private val mActionListener = object : TrafficImagesAdapter.ActionListener {
        override fun onImageClicked(image: TrafficImage) {
            TrafficImageDialogFragment.newInstance(image).show(fragmentManager, "traffic_image_dialog")
        }
    }


}