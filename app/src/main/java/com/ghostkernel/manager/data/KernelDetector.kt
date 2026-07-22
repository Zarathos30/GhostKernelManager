package com.ghostkernel.manager.data

import android.os.Build
import android.os.SystemClock
import android.util.Log

object KernelDetector {

    private const val TAG = "GhostKernelDetector"

    fun isGhostKernel(): Boolean {
        val osVer = System.getProperty("os.version") ?: ""
        Log.d(TAG, "os.version = $osVer")
        val detected = osVer.contains("GhostKernel", ignoreCase = true) ||
               osVer.contains("Kono-Ha", ignoreCase = true)
        Log.d(TAG, "GhostKernel detected = $detected")
        return detected
    }

    fun getVersionDebug(): String {
        val osVer = System.getProperty("os.version") ?: ""
        return "os.version: $osVer"
    }

    data class KernelInfo(
        val fullVersion: String,
        val localVersion: String,
        val kernelVersion: String,
        val arch: String,
        val uptime: String,
        val numCpus: Int,
        val cpuArch: String,
        val soc: String
    )

    private fun getProp(key: String): String {
        return try {
            val p = Runtime.getRuntime().exec(arrayOf("getprop", key))
            p.inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) { "" }
    }

    private fun resolveSoc(name: String): String {
        val map = mapOf(
            "sm8735" to "Snapdragon 8s Gen 4",
            "tuna" to "Snapdragon 8s Gen 4",
            "sm8650" to "Snapdragon 8 Gen 3",
            "pineapple" to "Snapdragon 8 Gen 3",
            "sm8635" to "Snapdragon 8s Gen 3",
            "cliffs" to "Snapdragon 8s Gen 3",
            "sm8550" to "Snapdragon 8 Gen 2",
            "kalama" to "Snapdragon 8 Gen 2",
            "sm8450" to "Snapdragon 8 Gen 1",
            "taro" to "Snapdragon 8 Gen 1",
            "sm8350" to "Snapdragon 888",
            "lahaina" to "Snapdragon 888",
            "sm8250" to "Snapdragon 865",
            "kona" to "Snapdragon 865",
        )
        return map[name.lowercase()] ?: name
    }

    fun getInfo(): KernelInfo {
        val osVer = System.getProperty("os.version") ?: "Unknown"
        val kernelVer = osVer.split("-").firstOrNull()?.trim() ?: osVer

        val localVer = if (osVer.contains("GhostKernel", ignoreCase = true)) "GhostKernel"
            else if (osVer.contains("Kono-Ha", ignoreCase = true)) "Kono-Ha-Kernel"
            else kernelVer

        val uptime = try {
            val up = SystemClock.elapsedRealtime() / 1000
            val hours = (up / 3600).toInt()
            val mins = ((up % 3600) / 60).toInt()
            "${hours}h ${mins}m"
        } catch (e: Exception) { "N/A" }

        val cpus = Runtime.getRuntime().availableProcessors()

        val soc = try {
            var hw = getProp("ro.soc.model")
            if (hw.isEmpty()) {
                val cpuinfo = SysFsManager.readLines("/proc/cpuinfo")
                hw = cpuinfo.firstOrNull { it.startsWith("Hardware") }?.substringAfter(":")?.trim() ?: ""
            }
            if (hw.isEmpty() || hw == "qcom") {
                hw = SysFsManager.read("/sys/devices/soc0/machine")
            }
            if (hw.isEmpty()) {
                hw = SysFsManager.read("/sys/devices/soc0/soc_id")
            }
            hw.ifEmpty { Build.HARDWARE ?: "Unknown" }
        } catch (e: Exception) { Build.HARDWARE ?: "Unknown" }

        return KernelInfo(
            fullVersion = osVer,
            localVersion = localVer,
            kernelVersion = kernelVer,
            arch = System.getProperty("os.arch") ?: "aarch64",
            uptime = uptime,
            numCpus = cpus,
            cpuArch = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
            soc = resolveSoc(soc)
        )
    }
}
