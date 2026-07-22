package com.ghostkernel.manager.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = BootPrefs(context)
            val script = buildInitScript(prefs)
            if (script.isNotEmpty()) {
                BootPrefs.runScript(script)
            }
        }
    }

    private fun buildInitScript(prefs: BootPrefs): String {
        val all = prefs.getAll()
        if (all.isEmpty()) return ""
        val sb = StringBuilder("#!/system/bin/sh\n")
        all.forEach { (key, value) ->
            val v = value as? String ?: return@forEach
            val path = key.replace("___", "/")
            sb.append("echo '$v' > $path\n")
        }
        return sb.toString()
    }
}
