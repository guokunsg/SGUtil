package com.gk.test

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry

/**
 *
 */
private class MockedLifecycle : Lifecycle() {
    override fun addObserver(observer: LifecycleObserver) {
    }

    override fun removeObserver(observer: LifecycleObserver) {
    }

    override fun getCurrentState(): State {
        return State.STARTED
    }
}

private class DummyLifecycleOwner() : LifecycleOwner {
    override fun getLifecycle(): Lifecycle {
        return MockedLifecycle()
    }
}

/**
 * A mocked LifecycleOwner
 */
class MockedLifecycleOwner(private val mLifecycle: LifecycleRegistry) : LifecycleOwner {
    override fun getLifecycle(): Lifecycle {
        return mLifecycle
    }

    /**
     * Chagne the state to resume so that observer can receive data event
     */
    fun changeToResumed() {
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }
}

/**
 * Create an LifecycleOwner for testing
 */
fun createMockedLifecycleOwner(): MockedLifecycleOwner {
    val dummyOwner = DummyLifecycleOwner()
    return MockedLifecycleOwner(LifecycleRegistry(dummyOwner))
}

