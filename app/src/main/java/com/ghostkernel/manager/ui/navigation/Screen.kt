package com.ghostkernel.manager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Cpu : Screen("cpu", "CPU", Icons.Default.Memory)
    data object Gpu : Screen("gpu", "GPU", Icons.Default.GraphicEq)
    data object Io : Screen("io", "I/O", Icons.Default.Storage)
    data object Tcp : Screen("tcp", "TCP", Icons.Default.Lan)
    data object Boot : Screen("boot", "Boot", Icons.Default.PowerSettingsNew)
}
