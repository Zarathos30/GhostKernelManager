package com.ghostkernel.manager.data

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object SysFsManager {

    private const val TAG = "GhostKM-SysFs"

    fun isRootAvailable(): Boolean {
        Log.d(TAG, "isRootAvailable: checking...")
        return try {
            val process = Runtime.getRuntime().exec("su -c echo root_ok")
            val exited = process.waitFor(5, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                Log.d(TAG, "isRootAvailable: timed out after 5s")
                return false
            }
            val ok = process.exitValue() == 0
            Log.d(TAG, "isRootAvailable: exit=${process.exitValue()} ok=$ok")
            ok
        } catch (e: Exception) {
            Log.e(TAG, "isRootAvailable: exception", e)
            false
        }
    }

    fun read(path: String): String {
        return try {
            if (needsRoot(path)) {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
                process.waitFor(5, TimeUnit.SECONDS)
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val text = reader.readText().trim()
                Log.d(TAG, "read(su:$path) = ${text.take(80)}")
                return text
            }
            if (path.startsWith("/proc")) {
                val process = Runtime.getRuntime().exec(arrayOf("cat", path))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val text = reader.readText().trim()
                Log.d(TAG, "read(proc:$path) = ${text.take(80)}")
                return text
            }
            val text = java.io.File(path).readText().trim()
            Log.d(TAG, "read($path) = ${text.take(80)}")
            text
        } catch (e: Exception) {
            Log.e(TAG, "read($path): ${e.message}")
            ""
        }
    }

    fun readLines(path: String): List<String> {
        return try {
            if (needsRoot(path)) {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
                process.waitFor(5, TimeUnit.SECONDS)
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val lines = reader.readLines().map { it.trim() }.filter { it.isNotEmpty() }
                Log.d(TAG, "readLines(su:$path) = ${lines.size} lines")
                return lines
            }
            if (path.startsWith("/proc")) {
                val process = Runtime.getRuntime().exec(arrayOf("cat", path))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val lines = reader.readLines().map { it.trim() }.filter { it.isNotEmpty() }
                Log.d(TAG, "readLines(proc:$path) = ${lines.size} lines")
                return lines
            }
            val lines = java.io.File(path).readLines().map { it.trim() }.filter { it.isNotEmpty() }
            Log.d(TAG, "readLines($path) = ${lines.size} lines")
            lines
        } catch (e: Exception) {
            Log.e(TAG, "readLines($path): ${e.message}")
            emptyList()
        }
    }

    fun write(path: String, value: String): Boolean {
        Log.d(TAG, "write($path, $value)")
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "echo '$value' > $path"))
            val exit = process.waitFor()
            Log.d(TAG, "write exit=$exit")
            exit == 0
        } catch (e: Exception) {
            Log.e(TAG, "write($path): exception", e)
            false
        }
    }

    private fun needsRoot(path: String): Boolean {
        return !path.startsWith("/proc") || path.startsWith("/proc/sys")
    }
}
