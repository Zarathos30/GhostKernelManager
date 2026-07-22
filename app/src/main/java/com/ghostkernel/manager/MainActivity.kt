package com.ghostkernel.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ghostkernel.manager.data.KernelDetector
import com.ghostkernel.manager.data.SysFsManager
import com.ghostkernel.manager.ui.navigation.NavGraph
import com.ghostkernel.manager.ui.navigation.Screen
import com.ghostkernel.manager.ui.screens.ContentCard
import com.ghostkernel.manager.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GhostKernelTheme {
                MainUI()
            }
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI() {
    var accessState by remember { mutableStateOf(0) } // 0=checking, 1=granted, 2=denied
    var denyReason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val ghost = KernelDetector.isGhostKernel()
            if (!ghost) {
                denyReason = "GhostKernel not detected.\nThis app only works with GhostKernel."
                accessState = 2
            } else {
                accessState = 1
            }
        }
    }

    when (accessState) {
        0 -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GhostCyan, strokeWidth = 2.dp)
                    Spacer(Modifier.height(16.dp))
                    Text("Checking access...", style = MaterialTheme.typography.bodyMedium, color = GhostGray)
                }
            }
        }
        1 -> {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val items = listOf(
                BottomNavItem(Screen.Dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                BottomNavItem(Screen.Cpu, Icons.Filled.Memory, Icons.Outlined.Memory),
                BottomNavItem(Screen.Gpu, Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq),
                BottomNavItem(Screen.Io, Icons.Filled.Storage, Icons.Outlined.Storage),
                BottomNavItem(Screen.Tcp, Icons.Filled.Lan, Icons.Outlined.Lan),
                BottomNavItem(Screen.Boot, Icons.Filled.PowerSettingsNew, Icons.Outlined.PowerSettingsNew),
            )

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = GhostDarkGray,
                        contentColor = GhostWhite,
                        tonalElevation = 0.dp,
                    ) {
                        items.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.screen.title,
                                        tint = if (selected) GhostCyan else GhostGray
                                    )
                                },
                                label = {
                                    Text(item.screen.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) GhostCyan else GhostGray)
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = GhostCyanDim.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                },
                containerColor = GhostBlack
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    NavGraph(navController)
                }
            }
        }
        2 -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ContentCard {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⛔", style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(16.dp))
                        Text("Access Denied", style = MaterialTheme.typography.titleLarge, color = GhostRed)
                        Spacer(Modifier.height(12.dp))
                        Text(denyReason, style = MaterialTheme.typography.bodyMedium, color = GhostGray)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                accessState = 0
                                denyReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GhostCyanDim)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
