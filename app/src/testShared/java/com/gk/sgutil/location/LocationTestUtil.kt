@file:Suppress("UNUSED_PARAMETER")

package com.gk.sgutil.location

import android.content.Context
import android.os.Looper
import org.mockito.Mockito

// Internal variable names to inject functions which are only available in Android
const val FN_PERMISSION_CHECKER = "fnCheckPermission"
const val FN_GET_LOOPER = "fnGetLooper"

const val VAR_LOCATION_CLIENT = "mLocationClient"

// Mock function to always have permission
fun alwaysHasPermission(c: Context, p: Array<String>): Boolean {
    return true
}

// Mock function to always have no permission
fun alwaysNoPermission(c: Context, p: Array<String>): Boolean {
    return false
}

// Mock Android function in order to continue
fun getLooper(): Looper? {
    return Mockito.mock(Looper::class.java)
}
