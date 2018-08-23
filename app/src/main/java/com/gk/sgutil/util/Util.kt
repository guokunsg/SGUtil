package com.gk.sgutil.util

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.gk.sgutil.R
import com.gk.sgutil.SGUtilException

/**
 * Check whether the app has granted the run-time permission
 */
fun hasPermission(context: Context, permissions: Array<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
            return false
    }
    return true
}

// Convert the error into proper error message
fun getErrorMessage(error: Throwable, context: Context): String {
    if (error is SGUtilException) {
        val busException = error
        when (busException.errCode) {
            SGUtilException.ErrorCode.NoLocationPermission ->
                return context.getString(R.string.error_require_location_permission)
            SGUtilException.ErrorCode.NoLocationAvailable ->
                return context.getString(R.string.error_no_location)
            SGUtilException.ErrorCode.NetworkError ->
                return context.getString(R.string.error_network_error)
            SGUtilException.ErrorCode.Unknown ->
                return context.getString(R.string.error_unknown)
            SGUtilException.ErrorCode.LocationServiceError ->
                return context.getString(R.string.error_location_service_error)
        }
    }
    if (error.message == null) {
        return error.toString()
    } else {
        return error.message!!
    }
}