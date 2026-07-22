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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ghostkernel.manager.ui.navigation.NavGraph
import com.ghostkernel.manager.ui.navigation.Screen
import com.ghostkernel.manager.ui.theme.*

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
