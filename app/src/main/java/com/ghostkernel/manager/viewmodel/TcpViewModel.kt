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

class TcpViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = BootPrefs(application)

    private val _current = MutableStateFlow("")
    val current: StateFlow<String> = _current

    private val _available = MutableStateFlow<List<String>>(emptyList())
    val available: StateFlow<List<String>> = _available

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _current.value = SysFsManager.read("/proc/sys/net/ipv4/tcp_congestion_control")
            _available.value = SysFsManager.read("/proc/sys/net/ipv4/tcp_available_congestion_control")
                .split(" ").filter { it.isNotEmpty() }
        }
    }

    fun setAlgorithm(alg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            SysFsManager.write("/proc/sys/net/ipv4/tcp_congestion_control", alg)
            refresh()
        }
    }

    fun saveBoot() {
        val key = "/proc___sys___net___ipv4___tcp_congestion_control"
        prefs.set(key, _current.value)
    }
}
