package com.n1ck120.mqtt

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {
        private lateinit var sharedPreferences: SharedPreferences

        fun init(context: Context) {
            sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        }

        fun saveString(key: String, value: String) {
            sharedPreferences.edit().putString(key, value).apply()
        }

        fun getString(key: String, defaultValue: String? = null): String? {
            return sharedPreferences.getString(key, defaultValue)
        }

        fun keyExists(key: String): Boolean {
            return sharedPreferences.contains(key)
        }

        fun fileHaveData(): Boolean {
            return sharedPreferences.all.isNotEmpty()
        }

        fun removeKey(key: String) {
            sharedPreferences.edit().remove(key).apply()
        }

        fun clearAllData() {
            sharedPreferences.edit().clear().apply()
        }
}