package com.gk.sgutil.bus.view

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import com.gk.sgutil.MainApplication
import com.gk.sgutil.bus.viewmodel.BusViewModelFactory
import com.gk.sgutil.util.getErrorMessage
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.swipe_refresh_recyclerview.*
import javax.inject.Inject

/**
 * Base fragment with dagger injection.
 * Didn't use DaggerFragment because it is only injected when attached to Activity.
 */
open class BaseFragment : Fragment(), HasSupportFragmentInjector {

    @Inject
    lateinit var injector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mViewModelFactory: BusViewModelFactory // Need this to do ViewModel injection

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return injector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidSupportInjection.inject(this)
    }

    // Display the error to the user and handle the action when the user accepts it
    protected open fun showError(error: Throwable) {
        Snackbar.make(recycler_view, getErrorMessage(error, context!!), Snackbar.LENGTH_LONG)
                .show()
    }
}