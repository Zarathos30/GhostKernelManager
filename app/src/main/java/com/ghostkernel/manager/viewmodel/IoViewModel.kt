package com.ghostkernel.manager.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkernel.manager.data.BootPrefs
import com.ghostkernel.manager.data.SysFsManager
import kotlinx.coroutines.Dispatchers
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
            val devices = mutableListOf<IoDevice>()
            var blockNames = listOf<String>()
            try {
                val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls /sys/block"))
                val reader = java.io.BufferedReader(java.io.InputStreamReader(p.inputStream))
                blockNames = reader.readLines().map { it.trim() }.filter {
                    it.isNotEmpty() && !it.startsWith("loop") && !it.startsWith("ram") && !it.startsWith("dm-")
                }
            } catch (e: Exception) {
                Log.e("GhostKM-IO", "Failed to list /sys/block", e)
            }
            Log.d("GhostKM-IO", "Found ${blockNames.size} block devices: $blockNames")

            for (name in blockNames) {

                val schedStr = SysFsManager.read("/sys/block/$name/queue/scheduler")
                if (schedStr.isEmpty()) {
                    Log.d("GhostKM-IO", "$name: scheduler empty, skipping")
                    continue
                }

                val current = schedStr.split("[")
                    .getOrNull(1)?.split("]")?.firstOrNull()?.trim()
                    ?: schedStr.split(" ").firstOrNull()?.trim() ?: "none"
                val available = schedStr.replace("[", "").replace("]", "").split(" ").filter { it.isNotEmpty() }

                val raStr = SysFsManager.read("/sys/block/$name/queue/read_ahead_kb")
                val ra = raStr.toIntOrNull() ?: 128

                Log.d("GhostKM-IO", "$name: scheduler=$current, avail=$available, ra=$ra")
                devices.add(IoDevice(name, current, available, ra))
            }
            _devices.value = devices
        }
    }

    fun setScheduler(device: String, scheduler: String) {
        viewModelScope.launch(Dispatchers.IO) {
            SysFsManager.write("/sys/block/$device/queue/scheduler", scheduler)
            refresh()
        }
    }

    fun setReadAhead(device: String, kb: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            SysFsManager.write("/sys/block/$device/queue/read_ahead_kb", kb.toString())
            refresh()
        }
    }

    fun saveBoot(device: String, type: String, value: String) {
        val key = "/sys___block___${device}___queue___$type"
        prefs.set(key, value)
    }
}
