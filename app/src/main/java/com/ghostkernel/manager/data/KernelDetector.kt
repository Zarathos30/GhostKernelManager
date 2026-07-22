package com.ghostkernel.manager.data

import android.os.Build

object KernelDetector {

    fun isGhostKernel(): Boolean {
        val version = SysFsManager.read("/proc/version")
        return version.contains("GhostKernel", ignoreCase = true) ||
               version.contains("Kono-Ha", ignoreCase = true) ||
               version.contains("ghost", ignoreCase = true)
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
        val version = SysFsManager.read("/proc/version")
        val parts = version.split(" ")
        val kernelVer = parts.getOrElse(0) { "Unknown" }
        val localVer = if (version.contains("GhostKernel")) "GhostKernel"
            else if (version.contains("Kono-Ha")) "Kono-Ha-Kernel"
            else kernelVer

        val compiler = parts.dropWhile { it != "(gcc" && it != "(clang" && it != "(aosp" && it != "(neutron" }
            .take(4).joinToString(" ").removePrefix("(").removeSuffix(")")

        val hostname = parts.dropWhile { it != "(gcc" && !it.startsWith("(") || it == "(gcc" }.drop(1)
            .let { rest ->
                val idx = rest.indexOfFirst { it == ")" }
                if (idx >= 0) rest.take(idx).joinToString(" ") else "Unknown"
            }

        val uptime = try {
            val up = SysFsManager.read("/proc/uptime").split(" ").firstOrNull()?.toDoubleOrNull() ?: 0.0
            val hours = (up / 3600).toInt()
            val mins = ((up % 3600) / 60).toInt()
            "${hours}h ${mins}m"
        } catch (e: Exception) { "N/A" }

        val cpus = SysFsManager.read("/sys/devices/system/cpu/possible")
            .split(",").flatMap { range ->
                if (range.contains("-")) {
                    val (start, end) = range.split("-").map { it.toIntOrNull() ?: 0 }
                    (start..end).toList()
                } else listOf(range.toIntOrNull() ?: 0)
            }.distinct().size

        val soc = try {
            val cpuinfo = SysFsManager.readLines("/proc/cpuinfo")
            cpuinfo.firstOrNull { it.startsWith("Hardware") }?.substringAfter(":")?.trim() ?: Build.HARDWARE
        } catch (e: Exception) { Build.HARDWARE }

        return KernelInfo(
            fullVersion = version,
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
