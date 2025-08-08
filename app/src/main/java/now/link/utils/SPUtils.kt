package now.link.utils

import android.content.Context
import android.content.SharedPreferences
import now.link.App

object SPUtils {

    private val sp by lazy {
        App.instance.getSharedPreferences("Ulmaridae", Context.MODE_PRIVATE)
    }

    @JvmStatic
    fun setString(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }

    @JvmStatic
    fun getString(key: String): String {
        return sp.getString(key, "") ?: ""
    }

    @JvmStatic
    fun getString(key: String, defaultValue: String): String? {
        return sp.getString(key, defaultValue)
    }

    @JvmStatic
    fun setInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    @JvmStatic
    fun getInt(key: String): Int {
        return sp.getInt(key, 0)
    }

    @JvmStatic
    fun getInt(key: String, defaultValue: Int): Int {
        return sp.getInt(key, defaultValue)
    }

    @JvmStatic
    fun getLong(key: String): Long {
        return sp.getLong(key, 0)
    }

    @JvmStatic
    fun getLong(key: String, defaultValue: Long): Long {
        return sp.getLong(key, defaultValue)
    }

    @JvmStatic
    fun setLong(key: String, value: Long) {
        return sp.edit().putLong(key, value).apply()
    }

    @JvmStatic
    fun setBoolean(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    fun getBoolean(key: String): Boolean {
        return sp.getBoolean(key, false)
    }

    @JvmStatic
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sp.getBoolean(key, defaultValue)
    }

    @JvmStatic
    fun setFloat(key: String, value: Float) {
        sp.edit().putFloat(key, value).apply()
    }

    @JvmStatic
    fun getFloat(key: String): Float {
        return sp.getFloat(key, 0.0f)
    }

    @JvmStatic
    fun getFloat(key: String, defaultValue: Float): Float {
        return sp.getFloat(key, defaultValue)
    }

    @JvmStatic
    fun getBooleanWithContext(context: Context, key: String, defaultValue: Boolean): Boolean {
        return context.getSharedPreferences("Halo", Context.MODE_PRIVATE)
            .getBoolean(key, defaultValue)
    }

    /**
     * 使用传入的 SP 来存储 KV
     */
    @JvmStatic
    fun setString(sharedPreferences: SharedPreferences, key: String, value: String? = null) {
        try {
            val commitStatus = sharedPreferences.edit().putString(key, value).commit()
            if (!commitStatus) {
                sharedPreferences.edit().putString(key, value).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sharedPreferences.edit().putString(key, value).apply()
        }
    }

    /**
     * 使用传入的 SP 来存储 KV
     */
    @JvmStatic
    fun setLong(sharedPreferences: SharedPreferences, key: String, value: Long) {
        try {
            val commitStatus = sharedPreferences.edit().putLong(key, value).commit()
            if (!commitStatus) {
                sharedPreferences.edit().putLong(key, value).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sharedPreferences.edit().putLong(key, value).apply()
        }
    }

    /**
     * 使用传入的 SP 来读取 KV
     */
    @JvmStatic
    fun getString(
        sharedPreferences: SharedPreferences,
        key: String,
        value: String? = null,
    ): String {
        return sharedPreferences.getString(key, "") ?: ""
    }

    /**
     * 使用传入的 SP 来读取 KV
     */
    @JvmStatic
    fun getLong(sharedPreferences: SharedPreferences, key: String, value: Long): Long {
        return sharedPreferences.getLong(key, value)
    }

    @JvmStatic
    fun setStringSet(key: String, values: Set<String>) {
        sp.edit().putStringSet(key, values).apply()
    }

    @JvmStatic
    fun getStringSet(key: String): Set<String> {
        return sp.getStringSet(key, HashSet()) ?: HashSet()
    }

    @JvmStatic
    fun getStringSet(key: String, defaultValues: Set<String>?): Set<String>? {
        return sp.getStringSet(key, defaultValues)
    }

    @JvmStatic
    fun remove(key: String) {
        val editor = sp.edit()
        editor.remove(key)
        editor.apply()
    }

    /**
     * 使用传入的 SP 来移除 KV
     */
    @JvmStatic
    fun remove(sharedPreferences: SharedPreferences, key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }
}