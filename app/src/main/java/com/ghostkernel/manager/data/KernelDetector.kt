package com.ghostkernel.manager.data

import android.os.Build
import android.util.Log

object KernelDetector {

    private const val TAG = "GhostKernelDetector"

    fun isGhostKernel(): Boolean {
        val version = SysFsManager.read("/proc/version")
        val osVer = System.getProperty("os.version") ?: ""
        Log.d(TAG, "/proc/version = $version")
        Log.d(TAG, "os.version = $osVer")
        val combined = "$version $osVer"
        val detected = combined.contains("GhostKernel", ignoreCase = true) ||
               combined.contains("Kono-Ha", ignoreCase = true)
        Log.d(TAG, "GhostKernel detected = $detected")
        return detected
    }

    fun getVersionDebug(): String {
        val osVer = System.getProperty("os.version") ?: ""
        val version = SysFsManager.read("/proc/version")
        return "os.version: $osVer\n/proc/version: ${version.ifEmpty { "(permission denied)" }}"
    }

    data class KernelInfo(
        val fullVersion: String,
        val localVersion: String,
        val kernelVersion: String,
        val arch: String,
        val compiler: String,
        val hostname: String,
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

        val version = SysFsManager.read("/proc/version")
        val parts = version.split(" ")
        val compiler = if (version.isNotEmpty()) {
            parts.dropWhile { it != "(gcc" && it != "(clang" && it != "(aosp" && it != "(neutron" }
                .take(4).joinToString(" ").removePrefix("(").removeSuffix(")")
        } else "Unknown"

        val hostname = if (version.isNotEmpty()) {
            parts.dropWhile { it != "(gcc" && !it.startsWith("(") || it == "(gcc" }.drop(1)
                .let { rest ->
                    val idx = rest.indexOfFirst { it == ")" }
                    if (idx >= 0) rest.take(idx).joinToString(" ") else "Unknown"
                }
        } else "Unknown"

        val uptime = try {
            val up = SysFsManager.read("/proc/uptime").split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
            if (up > 0) {
                val hours = (up / 3600).toInt()
                val mins = ((up % 3600) / 60).toInt()
                "${hours}h ${mins}m"
            } else "N/A"
        } catch (e: Exception) { "N/A" }

        val cpus = Runtime.getRuntime().availableProcessors()

        val soc = try {
            val cpuinfo = SysFsManager.readLines("/proc/cpuinfo")
            val hw = cpuinfo.firstOrNull { it.startsWith("Hardware") }?.substringAfter(":")?.trim()
            hw?.ifEmpty { null } ?: Build.HARDWARE ?: "Unknown"
        } catch (e: Exception) { Build.HARDWARE ?: "Unknown" }

        return KernelInfo(
            fullVersion = osVer,
            localVersion = localVer,
            kernelVersion = kernelVer,
            arch = System.getProperty("os.arch") ?: "aarch64",
            compiler = compiler.ifEmpty { "Unknown" },
            hostname = hostname.ifEmpty { "Unknown" },
            uptime = uptime,
            numCpus = cpus,
            cpuArch = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
            soc = soc
        )
    }
}
