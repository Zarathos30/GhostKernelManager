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

data class IoDevice(
    val name: String,
    val scheduler: String,
    val availableSchedulers: List<String>,
    val readAheadKb: Int,
)

class IoViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = BootPrefs(application)

    private val _devices = MutableStateFlow<List<IoDevice>>(emptyList())
    val devices: StateFlow<List<IoDevice>> = _devices

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            var blockNames = listOf<String>()
            try {
                val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls /sys/block"))
                val reader = java.io.BufferedReader(java.io.InputStreamReader(p.inputStream))
                blockNames = reader.readLines().map { it.trim() }.filter {
                    it.isNotEmpty() && !it.startsWith("loop") && !it.startsWith("ram") && !it.startsWith("dm-")
                }
            } catch (_: Exception) {
            }

            val devices = coroutineScope {
                blockNames.map { name ->
                    async {
                        val schedStr = SysFsManager.read("/sys/block/$name/queue/scheduler")
                        if (schedStr.isEmpty()) return@async null

                        val raDeferred = async { SysFsManager.read("/sys/block/$name/queue/read_ahead_kb") }

                        val current = schedStr.split("[")
                            .getOrNull(1)?.split("]")?.firstOrNull()?.trim()
                            ?: schedStr.split(" ").firstOrNull()?.trim() ?: "none"
                        val available = schedStr.replace("[", "").replace("]", "").split(" ").filter { it.isNotEmpty() }
                        val ra = raDeferred.await().toIntOrNull() ?: 128

                        IoDevice(name, current, available, ra)
                    }
                }.awaitAll().filterNotNull()
            }
            _devices.value = devices
        }
    }

    fun setScheduler(device: String, scheduler: String) {
        viewModelScope.launch(Dispatchers.IO) {
            SysFsManager.write("/sys/block/$device/queue/scheduler", scheduler)
            saveBoot(device, "scheduler", scheduler)
            _devices.value = _devices.value.map { d ->
                if (d.name == device) d.copy(scheduler = scheduler) else d
            }
        }
    }

    fun setReadAhead(device: String, kb: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            SysFsManager.write("/sys/block/$device/queue/read_ahead_kb", kb.toString())
            saveBoot(device, "read_ahead_kb", kb.toString())
            _devices.value = _devices.value.map { d ->
                if (d.name == device) d.copy(readAheadKb = kb) else d
            }
        }
    }

    fun saveBoot(device: String, type: String, value: String) {
        val key = "/sys___block___${device}___queue___$type"
        prefs.set(key, value)
    }
}
