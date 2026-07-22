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
            val cpuinfo = SysFsManager.readLines("/proc/cpuinfo")
            var hw = cpuinfo.firstOrNull { it.startsWith("Hardware") }?.substringAfter(":")?.trim()
            if (hw.isNullOrEmpty() || hw == "qcom") {
                hw = cpuinfo.firstOrNull { it.startsWith("model name") }?.substringAfter(":")?.trim()
            }
            if (hw.isNullOrEmpty() || hw == "qcom") {
                hw = cpuinfo.firstOrNull { it.startsWith("Processor") }?.substringAfter(":")?.trim()
            }
            hw?.ifEmpty { null } ?: Build.HARDWARE ?: "Unknown"
        } catch (e: Exception) { Build.HARDWARE ?: "Unknown" }

        return KernelInfo(
            fullVersion = osVer,
            localVersion = localVer,
            kernelVersion = kernelVer,
            arch = System.getProperty("os.arch") ?: "aarch64",
            uptime = uptime,
            numCpus = cpus,
            cpuArch = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
            soc = soc
        )
    }
}
