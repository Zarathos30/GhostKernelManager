package com.ghostkernel.manager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ghostkernel.manager.data.KernelDetector
import com.ghostkernel.manager.data.SysFsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

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
            if (_isGhostKernel.value) {
                _kernelInfo.value = KernelDetector.getInfo()
            }
        }
    }
}
