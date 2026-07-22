package com.ghostkernel.manager.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.io.File

class BootPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ghost_boot", Context.MODE_PRIVATE)

    fun get(key: String): String = prefs.getString(key, "") ?: ""
    fun set(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun setMany(pairs: List<Pair<String, String>>) {
        prefs.edit().apply {
            pairs.forEach { putString(it.first, it.second) }
            apply()
        }
    }
    fun getAll(): Map<String, *> = prefs.all
    fun clear() = prefs.edit().clear().apply()

    companion object {
        fun applyBootSettings() {
            val prefs = android.app.Application.getProcessName() // won't work standalone
            // Handled via runScript
        }

        fun runScript(script: String): Boolean {
            return try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", script))
                process.waitFor() == 0
            } catch (e: Exception) {
                false
            }
        }
    }
}
