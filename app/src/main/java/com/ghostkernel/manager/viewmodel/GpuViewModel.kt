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

            for (base in paths) {
                val govPath = "$base/devfreq/governor"
                val availGovPath = "$base/devfreq/available_governors"
                val gov = SysFsManager.read(govPath)
                if (gov.isNotEmpty()) {
                    _gpuPath.value = "$base/devfreq"
                    val availGov = SysFsManager.read(availGovPath).split(" ").filter { it.isNotEmpty() }
                    val minFreq = SysFsManager.read("$base/devfreq/min_freq").toLongOrNull() ?: 0L
                    val maxFreq = SysFsManager.read("$base/devfreq/max_freq").toLongOrNull() ?: 0L
                    val curFreq = SysFsManager.read("$base/devfreq/cur_freq").toLongOrNull() ?: 0L
                    val availFreq = SysFsManager.read("$base/devfreq/available_frequencies")
                        .split(" ").mapNotNull { it.toLongOrNull() }.filter { it > 0 }

                    _gpu.value = GpuInfo("Adreno", gov, availGov, minFreq, maxFreq, curFreq, availFreq)
                    return@launch
                }
            }

            // Try simpler paths
            for (base in paths) {
                val gov = SysFsManager.read("$base/gpu_governor")
                    .ifEmpty { SysFsManager.read("$base/governor") }
                if (gov.isNotEmpty()) {
                    _gpuPath.value = base
                    _gpu.value = GpuInfo("Adreno", gov, emptyList(), 0, 0, 0, emptyList())
                    return@launch
                }
            }

            _gpu.value = null
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
