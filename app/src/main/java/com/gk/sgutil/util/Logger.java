package com.gk.sgutil.util;

import android.util.Log;

/**
 *
 */
public class Logger {

    private static final String TAG = "SGUTIL";

    public static void debug(String msg) {
        Log.d(TAG, msg);
    }

    public static void error(String msg) {
        Log.e(TAG, msg);
    }

    public static void error(String msg, Throwable t) {
        Log.e(TAG, msg, t);
    }
}
