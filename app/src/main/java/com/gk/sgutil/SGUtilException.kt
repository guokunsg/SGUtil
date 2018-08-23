package com.gk.sgutil

import java.io.IOException

/**
 *
 */
class SGUtilException(val errCode: ErrorCode, msg: String?, cause: Throwable?): Exception(msg, cause) {

    enum class ErrorCode {
        Unknown,
        NoLocationPermission,
        NoLocationAvailable,
        NetworkError,
        LocationServiceError,
    }

    companion object {
        fun translateError(error: Throwable): SGUtilException {
            if (error is SGUtilException)
                return error
            if (error is IOException)
                return SGUtilException(SGUtilException.ErrorCode.NetworkError)
            return SGUtilException(ErrorCode.Unknown, error)
        }
    }

    constructor(errorCode: ErrorCode) : this(errorCode, null, null) {

    }

    constructor(errorCode: ErrorCode, cause: Throwable?) : this(errorCode, cause!!.message, cause) {

    }
}