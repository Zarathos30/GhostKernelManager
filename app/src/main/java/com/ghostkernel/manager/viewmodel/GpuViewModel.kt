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

data class GpuInfo(
    val model: String,
    val governor: String,
    val availableGovernors: List<String>,
    val minFreq: Long,
    val maxFreq: Long,
    val curFreq: Long,
    val availableFrequencies: List<Long>,
)

class GpuViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = BootPrefs(application)

    private val _gpu = MutableStateFlow<GpuInfo?>(null)
    val gpu: StateFlow<GpuInfo?> = _gpu

    private val _gpuPath = MutableStateFlow<String?>(null)

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val paths = listOf(
                "/sys/class/kgsl/kgsl-3d0",
                "/sys/class/kgsl/kgsl-2d0",
                "/sys/kernel/gpu",
                "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0",
            )

            val result = coroutineScope {
                paths.map { base ->
                    async {
                        val govPath = "$base/devfreq/governor"
                        val gov = SysFsManager.read(govPath)
                        if (gov.isEmpty()) return@async null

                        val availGovDeferred = async { SysFsManager.read("$base/devfreq/available_governors") }
                        val minFreqDeferred = async { SysFsManager.read("$base/devfreq/min_freq") }
                        val maxFreqDeferred = async { SysFsManager.read("$base/devfreq/max_freq") }
                        val curFreqDeferred = async { SysFsManager.read("$base/devfreq/cur_freq") }
                        val availFreqDeferred = async { SysFsManager.read("$base/devfreq/available_frequencies") }

                        val availGov = availGovDeferred.await().split(" ").filter { it.isNotEmpty() }
                        val minFreq = minFreqDeferred.await().toLongOrNull() ?: 0L
                        val maxFreq = maxFreqDeferred.await().toLongOrNull() ?: 0L
                        val curFreq = curFreqDeferred.await().toLongOrNull() ?: 0L
                        val availFreq = availFreqDeferred.await()
                            .split(" ").mapNotNull { it.toLongOrNull() }.filter { it > 0 }

                        GpuInfo("Adreno", gov, availGov, minFreq, maxFreq, curFreq, availFreq) to "$base/devfreq"
                    }
                }.awaitAll().filterNotNull().firstOrNull()
            }

            if (result != null) {
                val (info, devfreqPath) = result
                _gpuPath.value = devfreqPath
                _gpu.value = info
                return@launch
            }

            val fallback = coroutineScope {
                paths.map { base ->
                    async {
                        val gov = SysFsManager.read("$base/gpu_governor")
                            .ifEmpty { SysFsManager.read("$base/governor") }
                        if (gov.isNotEmpty()) GpuInfo("Adreno", gov, emptyList(), 0, 0, 0, emptyList()) to base
                        else null
                    }
                }.awaitAll().filterNotNull().firstOrNull()
            }

            if (fallback != null) {
                val (info, base) = fallback
                _gpuPath.value = base
                _gpu.value = info
            } else {
                _gpu.value = null
            }
        }
    }

    fun setGovernor(governor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _gpuPath.value?.let {
                SysFsManager.write("$it/governor", governor)
                refresh()
            }
        }
    }

    fun setMinFreq(freq: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _gpuPath.value?.let {
                SysFsManager.write("$it/min_freq", freq.toString())
                refresh()
            }
        }
    }

    fun setMaxFreq(freq: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _gpuPath.value?.let {
                SysFsManager.write("$it/max_freq", freq.toString())
                refresh()
            }
        }
    }

    fun saveBoot(type: String, value: String) {
        _gpuPath.value?.let { path ->
            val key = path.replace("/", "___") + "___$type"
            prefs.set(key, value)
        }
    }
}
