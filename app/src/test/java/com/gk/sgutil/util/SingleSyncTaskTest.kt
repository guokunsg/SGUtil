package com.gk.sgutil.util

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.concurrent.thread

/**
 *
 */
class SingleSyncTaskTest {

    @Before
    fun setup() {
        // Let RX initialize first
        Observable.create<Int> {
        }.subscribeOn(Schedulers.newThread()).subscribe()
    }

    // Start 10 new thread and run the task. All thread should get the same result
    fun runTask(task: SingleSyncTask<Int>, threadNo: Int): Array<Any?> {
        val result = arrayOfNulls<Any>(threadNo)
        for (i in 0 until result.size) {
            thread {
                task.runSyncData().subscribe({
                    result[i] = it
                }, {
                    result[i] = it
                })
            }
        }
        Thread.sleep(500)

        return result
    }

    @Test
    fun testSyncDataTask() {
        // Create a task which only sleep for a while and increase the count
        val task = object: SingleSyncTask<Int>() {
            var count = 0
            override fun syncData(): Int {
                Thread.sleep(200)
                count ++
                return count
            }
        }

        val result1 = runTask(task, 10)
        Assert.assertEquals("Expect only one task is actually running", 1, task.count)
        for (e in result1)
            assertEquals("All subsciption should receive the same result", 1, e)

        val result2 = runTask(task, 10)
        Assert.assertEquals("Expect only one task is actually running",2, task.count)
        for (e in result2)
            assertEquals("All subsciption should receive the same result",2, e)
    }

    @Test
    fun testSyncDataTaskWithException() {
        val msg = "dummy_exception"
        // Create a task which only sleep for a while and throw exception
        val task = object: SingleSyncTask<Int>() {
            override fun syncData(): Int {
                Thread.sleep(100)
                throw RuntimeException(msg)
            }
        }
        // Start 10 new thread and run the task. All thread should get the same result
        val result = runTask(task, 10)
        Thread.sleep(1000)

        for (e in result) {
            assertNotNull(e)
            assertEquals("Expect error is received", msg, (e as Exception).message)
        }

        try {
            task.runSyncData().blockingFirst()
            fail("Expect error is thrown")
        } catch (e: Exception) {
            assertEquals("Expect error is received", msg, e.message)
        }
    }
}