package com.ghostkernel.manager.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ghostkernel.manager.ui.screens.*
import com.ghostkernel.manager.viewmodel.*

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val factory = rememberFactory(context)

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            val vm: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(vm)
        }
        composable(Screen.Cpu.route) {
            val vm: CpuViewModel = viewModel(factory = factory)
            CpuScreen(vm)
        }
        composable(Screen.Gpu.route) {
            val vm: GpuViewModel = viewModel(factory = factory)
            GpuScreen(vm)
        }
        composable(Screen.Io.route) {
            val vm: IoViewModel = viewModel(factory = factory)
            IoScreen(vm)
        }
        composable(Screen.Tcp.route) {
            val vm: TcpViewModel = viewModel(factory = factory)
            TcpScreen(vm)
        }
        composable(Screen.Boot.route) {
            val vm: DashboardViewModel = viewModel(factory = factory)
            BootScreen(context, vm)
        }
    }
}

@Composable
fun rememberFactory(context: android.content.Context): ViewModelProvider.Factory {
    val app = context.applicationContext as Application
    return ViewModelProvider.AndroidViewModelFactory.getInstance(app)
}
