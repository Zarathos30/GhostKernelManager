package com.ghostkernel.manager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkernel.manager.data.BootPrefs
import com.ghostkernel.manager.data.SysFsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

            val clusterMap = mutableMapOf<Int, MutableList<Int>>()
            possible.forEach { cpu ->
                try {
                    val policy = SysFsManager.read("$cpuPath/cpu$cpu/cpufreq/related_cpus")
                        .ifEmpty { SysFsManager.read("$cpuPath/cpu$cpu/cpufreq/affected_cpus") }
                        .ifEmpty { "$cpu" }
                    val firstCpu = policy.split(" ").firstOrNull { it.isNotEmpty() }?.toIntOrNull() ?: cpu
                    clusterMap.getOrPut(firstCpu) { mutableListOf() }.add(cpu)
                } catch (_: Exception) {
                    clusterMap.getOrPut(cpu) { mutableListOf() }.add(cpu)
                }
            }

            val clusterList = coroutineScope {
                clusterMap.entries.mapIndexed { idx, (leader, cpus) ->
                    async {
                        val govPath = "$cpuPath/cpu$leader/cpufreq"
                        val governorDeferred = async { SysFsManager.read("$govPath/scaling_governor") }
                        val availGovDeferred = async { SysFsManager.read("$govPath/scaling_available_governors") }
                        val minFreqDeferred = async { SysFsManager.read("$govPath/scaling_min_freq") }
                        val maxFreqDeferred = async { SysFsManager.read("$govPath/scaling_max_freq") }
                        val curFreqDeferred = async { SysFsManager.read("$govPath/scaling_cur_freq") }
                        val availFreqDeferred = async { SysFsManager.read("$govPath/scaling_available_frequencies") }

                        val governor = governorDeferred.await()
                        val availGov = availGovDeferred.await().split(" ").filter { it.isNotEmpty() }
                        val minFreq = minFreqDeferred.await().toLongOrNull() ?: 0L
                        val maxFreq = maxFreqDeferred.await().toLongOrNull() ?: 0L
                        val curFreq = curFreqDeferred.await().toLongOrNull() ?: 0L
                        val availFreq = availFreqDeferred.await()
                            .split(" ").mapNotNull { it.toLongOrNull() }.filter { it > 0 }

                        CpuCluster(idx, cpus, governor, availGov, minFreq, maxFreq, curFreq, availFreq)
                    }
                }.awaitAll()
            }

            _clusters.value = clusterList
        }
    }

    fun setGovernor(leaderCpu: Int, governor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "/sys/devices/system/cpu/cpu$leaderCpu/cpufreq/scaling_governor"
            SysFsManager.write(path, governor)
            saveBoot(leaderCpu, "governor", governor)
            _clusters.value = _clusters.value.map { c ->
                if (c.cpus.first() == leaderCpu) c.copy(governor = governor) else c
            }
        }
    }

    fun setMinFreq(leaderCpu: Int, freq: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "/sys/devices/system/cpu/cpu$leaderCpu/cpufreq/scaling_min_freq"
            SysFsManager.write(path, freq.toString())
            saveBoot(leaderCpu, "min_freq", freq.toString())
            _clusters.value = _clusters.value.map { c ->
                if (c.cpus.first() == leaderCpu) c.copy(minFreq = freq) else c
            }
        }
    }

    fun setMaxFreq(leaderCpu: Int, freq: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "/sys/devices/system/cpu/cpu$leaderCpu/cpufreq/scaling_max_freq"
            SysFsManager.write(path, freq.toString())
            saveBoot(leaderCpu, "max_freq", freq.toString())
            _clusters.value = _clusters.value.map { c ->
                if (c.cpus.first() == leaderCpu) c.copy(maxFreq = freq) else c
            }
        }
    }

    fun saveBoot(leaderCpu: Int, type: String, value: String) {
        val key = "/sys___devices___system___cpu___cpu${leaderCpu}___cpufreq___scaling_${type}"
        prefs.set(key, value)
    }
}
