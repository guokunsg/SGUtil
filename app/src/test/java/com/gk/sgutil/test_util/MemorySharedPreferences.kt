package com.gk.sgutil.test_util

import android.content.SharedPreferences
import android.support.v4.util.ArrayMap

@Suppress("UNCHECKED_CAST")
class MemorySharedPreferences : SharedPreferences, SharedPreferences.Editor {
    val map = ArrayMap<String, Any>()

    override fun contains(key: String?): Boolean {
        return map.contains(key)
    }

    override fun unregisterOnSharedPreferenceChangeListener(p0: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw NotImplementedError()
    }

    override fun getAll(): MutableMap<String, *> {
        throw NotImplementedError()
    }

    fun <T> get(key: String?, def: T?): T? {
        val ret = map.get(key)
        return if (ret == null) def else ret as T
    }

    override fun getBoolean(key: String?, def: Boolean): Boolean {
        return get(key, def)!!
    }

    override fun getInt(key: String?, def: Int): Int {
        return get(key, def)!!
    }

    override fun getLong(key: String?, def: Long): Long {
        return get(key, def)!!
    }

    override fun getFloat(key: String?, def: Float): Float {
        return get(key, def)!!
    }

    override fun getString(key: String?, def: String?): String? {
        return get(key, def)
    }

    override fun getStringSet(key: String?, def: MutableSet<String>?): MutableSet<String> {
        throw NotImplementedError()
    }

    override fun edit(): SharedPreferences.Editor {
        return this
    }

    override fun registerOnSharedPreferenceChangeListener(p0: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw NotImplementedError()
    }

    /**
     * Functions for editor
     */
    override fun clear(): SharedPreferences.Editor {
        map.clear()
        return this
    }

    override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
        map[key] = value
        return this
    }

    override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
        map[key] = value
        return this
    }

    override fun remove(key: String?): SharedPreferences.Editor {
        map.remove(key)
        return this
    }

    override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
        map[key] = value
        return this
    }

    override fun putStringSet(p0: String?, p1: MutableSet<String>?): SharedPreferences.Editor {
        throw NotImplementedError()
    }

    override fun commit(): Boolean {
        return true
    }

    override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
        map[key] = value
        return this
    }

    override fun apply() {
    }

    override fun putString(key: String?, value: String?): SharedPreferences.Editor {
        map[key] = value
        return this
    }
}
