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
            val blockDir = java.io.File("/sys/block")
            val devices = (blockDir.listFiles() ?: emptyArray()).mapNotNull { dir ->
                val name = dir.name
                val schedFile = java.io.File(dir, "queue/scheduler")
                if (!schedFile.exists()) return@mapNotNull null
                val schedStr = SysFsManager.read(schedFile.absolutePath)
                val parts = schedStr.replace("[", "").replace("]", "").split(" ")
                val available = schedStr.split(" ").map { it.replace("[", "").replace("]", "") }.filter { it.isNotEmpty() }
                val current = schedStr.split("[")
                    .getOrNull(1)?.split("]")?.firstOrNull()?.trim() ?: parts.firstOrNull() ?: "none"
                val ra = SysFsManager.read("${dir.absolutePath}/queue/read_ahead_kb").toIntOrNull() ?: 128
                IoDevice(name, current, available, ra)
            }
            _devices.value = devices
        }
    }

    fun setScheduler(device: String, scheduler: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "/sys/block/$device/queue/scheduler"
            SysFsManager.write(path, scheduler)
            refresh()
        }
    }

    fun setReadAhead(device: String, kb: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = "/sys/block/$device/queue/read_ahead_kb"
            SysFsManager.write(path, kb.toString())
            refresh()
        }
    }

    fun saveBoot(device: String, type: String, value: String) {
        val key = "/sys___block___${device}___queue___$type"
        prefs.set(key, value)
    }
}
