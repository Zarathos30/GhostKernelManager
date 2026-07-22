package com.ghostkernel.manager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkernel.manager.data.BootPrefs
import com.ghostkernel.manager.data.SysFsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CpuCluster(
    val index: Int,
    val cpus: List<Int>,
    val governor: String,
    val availableGovernors: List<String>,
    val minFreq: Long,
    val maxFreq: Long,
    val curFreq: Long,
    val availableFrequencies: List<Long>,
)

class CpuViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = BootPrefs(application)

    private val _clusters = MutableStateFlow<List<CpuCluster>>(emptyList())
    val clusters: StateFlow<List<CpuCluster>> = _clusters

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val cpuPath = "/sys/devices/system/cpu"
            val possible = SysFsManager.read("$cpuPath/possible")
                .split(",").flatMap { range ->
                    if (range.contains("-")) {
                        val (s, e) = range.split("-").map { it.toIntOrNull() ?: 0 }
                        (s..e).toList()
                    } else listOf(range.toIntOrNull() ?: 0)
                }.distinct()

            val clusters = mutableMapOf<Int, MutableList<Int>>()
            possible.forEach { cpu ->
                try {
                    val policy = SysFsManager.read("$cpuPath/cpu$cpu/cpufreq/related_cpus")
                        .ifEmpty { SysFsManager.read("$cpuPath/cpu$cpu/cpufreq/affected_cpus") }
                        .ifEmpty { "$cpu" }

                    val firstCpu = policy.split(" ").firstOrNull { it.isNotEmpty() }?.toIntOrNull() ?: cpu
                    clusters.getOrPut(firstCpu) { mutableListOf() }.add(cpu)
                } catch (e: Exception) {
                    clusters.getOrPut(cpu) { mutableListOf() }.add(cpu)
                }
            }

            val clusterList = clusters.entries.mapIndexed { idx, (leader, cpus) ->
                val govPath = "$cpuPath/cpu$leader/cpufreq"
                val governor = SysFsManager.read("$govPath/scaling_governor")
                val availGov = SysFsManager.read("$govPath/scaling_available_governors")
                    .split(" ").filter { it.isNotEmpty() }

                val minFreq = SysFsManager.read("$govPath/scaling_min_freq").toLongOrNull() ?: 0L
                val maxFreq = SysFsManager.read("$govPath/scaling_max_freq").toLongOrNull() ?: 0L
                val curFreq = SysFsManager.read("$govPath/scaling_cur_freq").toLongOrNull()
                    ?: SysFsManager.read("$govPath/cpuinfo_cur_freq").toLongOrNull() ?: 0L
                val availFreq = SysFsManager.read("$govPath/scaling_available_frequencies")
                    .split(" ").mapNotNull { it.toLongOrNull() }.filter { it > 0 }

                CpuCluster(idx, cpus, governor, availGov, minFreq, maxFreq, curFreq, availFreq)
            }

            _clusters.value = clusterList
        }
    }

    fun setGovernor(leaderCpu: Int, governor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "/sys/devices/system/cpu/cpu$leaderCpu/cpufreq/scaling_governor"
            val ok = SysFsManager.write(path, governor)
            if (ok) refresh()
            else println("Failed to set governor: $path = $governor")
        }
    }

    fun setMinFreq(leaderCpu: Int, freq: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "/sys/devices/system/cpu/cpu$leaderCpu/cpufreq/scaling_min_freq"
            SysFsManager.write(path, freq.toString())
            refresh()
        }
    }

    fun setMaxFreq(leaderCpu: Int, freq: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "/sys/devices/system/cpu/cpu$leaderCpu/cpufreq/scaling_max_freq"
            SysFsManager.write(path, freq.toString())
            refresh()
        }
    }

    fun saveBoot(leaderCpu: Int, type: String, value: String) {
        val key = "/sys___devices___system___cpu___cpu${leaderCpu}___cpufreq___scaling_${type}"
        prefs.set(key, value)
    }
}
