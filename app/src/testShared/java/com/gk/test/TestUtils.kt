package com.gk.test

import android.location.Location
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// Delta value for double comparison
const val DOUBLE_COMPARE_DELTA = 1e-15

/**
 * Utility functions to help testing
 */
class TestUtils {
    companion object {
        /**
         * Set the private field in the target object
         */
        fun setPrivateField(targetObject: Any, fieldName: String, value: Any) {
            val field = targetObject.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            try {
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
            } catch (e: Throwable) {
                // Android does not have modifiers field. Just ignore the error
            }
            field.set(targetObject, value)
        }

        fun getPrivateField(targetObject: Any, fieldName: String): Any {
            val field = targetObject.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            return field.get(targetObject)
        }

        /**
         * Run the function in another thread
         */
        fun runAsAsync(fn: () -> Unit) {
            Thread(fn).start()
        }

        /**
         * Sleep for a random time between minTime and maxTime
         */
        fun randomSleep(minTime: Int = 20, maxTime: Int = 100) {
            val time = SecureRandom().nextInt(maxTime - minTime) + minTime
            Thread.sleep(time.toLong())
        }
    }
}

/** Helper class to store the testing result in an asynchronous operation. */
class TestResult<T>(var data: T? = null, var error: Throwable? = null) {
    /** The count down latch to wait for data or error. Can be replaced. */
    var latch = CountDownLatch(1)
    /** Set operation has been done. Count down by 1 in the latch. */
    fun done(data: T?, error: Throwable?) {
        this.data = data
        this.error = error
        latch.countDown()
    }
    /** Wait for done being called or timeout. */
    fun waitForEvent(timeout: Long = 3000) {
        latch.await(timeout, TimeUnit.MILLISECONDS)
        Thread.sleep(50) // Sleep for a while to wait for data update
    }
}