package com.ghostkernel.manager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ghostkernel.manager.ui.screens.*
import com.ghostkernel.manager.viewmodel.*

@Composable
fun NavGraph(
    navController: NavHostController,
    dashboardVm: DashboardViewModel,
    cpuVm: CpuViewModel,
    gpuVm: GpuViewModel,
    ioVm: IoViewModel,
    tcpVm: TcpViewModel,
) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(dashboardVm)
        }
        composable(Screen.Cpu.route) {
            CpuScreen(cpuVm)
        }
        composable(Screen.Gpu.route) {
            GpuScreen(gpuVm)
        }
        composable(Screen.Io.route) {
            IoScreen(ioVm)
        }
        composable(Screen.Tcp.route) {
            TcpScreen(tcpVm)
        }
        composable(Screen.Boot.route) {
            BootScreen(context, dashboardVm)
        }
    }
}
