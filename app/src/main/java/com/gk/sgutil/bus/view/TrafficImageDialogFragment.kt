package com.gk.sgutil.bus.view

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gk.sgutil.R
import com.gk.sgutil.bus.model.TrafficImage
import com.gk.sgutil.location.AddressFinder
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.fragment_traffic_image_dialog.*
import javax.inject.Inject

/**
 *
 */
class TrafficImageDialogFragment : DialogFragment(), HasSupportFragmentInjector {

    @Inject
    lateinit var injector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mAddressFinder: AddressFinder

    companion object {
        const val ARG_KEY_TRAFFIC_IMAGE = "key_traffic_image"

        fun newInstance(trafficImage: TrafficImage): TrafficImageDialogFragment {
            val fragment = TrafficImageDialogFragment()
            val bundle = Bundle()
            bundle.putString(ARG_KEY_TRAFFIC_IMAGE, Gson().toJson(trafficImage))
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var mTrafficImage: TrafficImage

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return injector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidSupportInjection.inject(this)

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentFullScreen)
        mTrafficImage = Gson().fromJson<TrafficImage>(arguments!!.getString(ARG_KEY_TRAFFIC_IMAGE), TrafficImage::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_traffic_image_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image.setImageURI(mTrafficImage.imageLink)
        val location = LatLng(mTrafficImage.latitude, mTrafficImage.longitude)
        val address = mAddressFinder.getAddress(location)
        txt_address.text = AddressFinder.formatAddress(location, address)
    }
}