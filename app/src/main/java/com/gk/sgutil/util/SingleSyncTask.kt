package com.gk.sgutil.util

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Run only one data synchronization at the same time.
 */
abstract class SingleSyncTask<T> {
    private var task: Observable<T>? = null

    /**
     * Implement the synchronization in this function
     * @return
     *      The number of records which are synchronized
     */
    protected abstract fun syncData(): T

    /**
     * Start a synchronization on a new thread.
     * If there is no task running, a new task will be created and running regardless of
     * whether the returned Observable is subscribed.
     * If there is already a task running, no new task will be created and Observable to the
     * old task is returned. No new task is running when the returned Observable is subscribed
     * @return
     *      Observable contains the return value of the synchronization function
     */
    @Synchronized
    fun runSyncData(): Observable<T> {
        if (task == null) {
            task = Observable.create<T> {
                try {
                    it.onNext(syncData())
                } catch (t: Throwable) {
                    it.onError(t)
                } finally {
                    syncFinished()
                    it.onComplete()
                }
            }!!.cache() // To run only once
            // Subscribe to make it run
            task!!.subscribeOn(Schedulers.newThread()).subscribe({
            }, { /* Just ignore the error */ })
        }
        return task!!
    }

    /**
     * Set there is no task running
     */
    @Synchronized
    private fun syncFinished() {
        task = null
    }
}