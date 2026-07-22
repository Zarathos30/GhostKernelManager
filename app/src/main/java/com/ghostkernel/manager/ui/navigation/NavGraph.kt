package com.ghostkernel.manager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ghostkernel.manager.ui.screens.*
import com.ghostkernel.manager.viewmodel.*

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            val vm: DashboardViewModel = viewModel(androidViewModelFactory = defaultViewModelFactory(context))
            DashboardScreen(vm)
        }
        composable(Screen.Cpu.route) {
            val vm: CpuViewModel = viewModel(androidViewModelFactory = defaultViewModelFactory(context))
            CpuScreen(vm)
        }
        composable(Screen.Gpu.route) {
            val vm: GpuViewModel = viewModel(androidViewModelFactory = defaultViewModelFactory(context))
            GpuScreen(vm)
        }
        composable(Screen.Io.route) {
            val vm: IoViewModel = viewModel(androidViewModelFactory = defaultViewModelFactory(context))
            IoScreen(vm)
        }
        composable(Screen.Tcp.route) {
            val vm: TcpViewModel = viewModel(androidViewModelFactory = defaultViewModelFactory(context))
            TcpScreen(vm)
        }
        composable(Screen.Boot.route) {
            val vm: DashboardViewModel = viewModel(androidViewModelFactory = defaultViewModelFactory(context))
            BootScreen(context, vm)
        }
    }
}

fun defaultViewModelFactory(context: android.content.Context): androidx.lifecycle.ViewModelProvider.Factory {
    return (context as? androidx.lifecycle.ViewModelStoreOwner)
        ?.let { androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(it.applicationContext as android.app.Application) }
        ?: androidx.lifecycle.ViewModelProvider.NewInstanceFactory()
}
