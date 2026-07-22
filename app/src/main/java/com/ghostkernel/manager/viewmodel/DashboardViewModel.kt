package com.ghostkernel.manager.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkernel.manager.data.KernelDetector
import com.ghostkernel.manager.data.SysFsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    companion object { private const val TAG = "GhostKM-Dashboard" }

    private val _kernelInfo = MutableStateFlow(KernelDetector.KernelInfo("", "", "", "", "", 0, "", ""))
    val kernelInfo: StateFlow<KernelDetector.KernelInfo> = _kernelInfo

    private val _isGhostKernel = MutableStateFlow(false)
    val isGhostKernel: StateFlow<Boolean> = _isGhostKernel

    private val _rootAvailable = MutableStateFlow(false)
    val rootAvailable: StateFlow<Boolean> = _rootAvailable

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _isGhostKernel.value = KernelDetector.isGhostKernel()
            Log.d(TAG, "isGhostKernel = ${_isGhostKernel.value}")
            if (_isGhostKernel.value) {
                _kernelInfo.value = KernelDetector.getInfo()
                Log.d(TAG, "kernelInfo = ${_kernelInfo.value.kernelVersion}")
            }
        }
    }
}
