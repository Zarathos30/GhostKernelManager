package com.ghostkernel.manager.data

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object SysFsManager {

    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c echo root_ok")
            val exited = process.waitFor(5, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                return false
            }
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun read(path: String): String {
        return try {
            val process = if (path.startsWith("/proc") || path.startsWith("/sys/class") && !needsRoot(path)) {
                Runtime.getRuntime().exec(arrayOf("cat", path))
            } else {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
            }
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readText().trim()
        } catch (e: Exception) {
            ""
        }
    }

    fun readLines(path: String): List<String> {
        return try {
            val process = if (path.startsWith("/proc") || !needsRoot(path)) {
                Runtime.getRuntime().exec(arrayOf("cat", path))
            } else {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
            }
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun write(path: String, value: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "echo '$value' > $path"))
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun needsRoot(path: String): Boolean {
        return !path.startsWith("/proc")
    }
}
