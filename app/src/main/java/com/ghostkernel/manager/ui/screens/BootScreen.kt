package com.ghostkernel.manager.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghostkernel.manager.data.BootPrefs
import com.ghostkernel.manager.ui.theme.*
import com.ghostkernel.manager.viewmodel.DashboardViewModel

@Composable
fun BootScreen(context: Context, vm: DashboardViewModel) {
    val prefs = remember { BootPrefs(context) }
    var count by remember { mutableIntStateOf(prefs.getAll().size) }
    val info by vm.kernelInfo.collectAsState()
    val isGhost by vm.isGhostKernel.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Apply on Boot", style = MaterialTheme.typography.headlineMedium, color = GhostCyan)
        Spacer(Modifier.height(16.dp))

        ContentCard {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Saved Boot Settings: $count",
                    style = MaterialTheme.typography.bodyMedium, color = GhostAmber)
                Text("Settings are applied automatically at boot via a receiver.",
                    style = MaterialTheme.typography.bodySmall, color = GhostGray)
                Text("Use the save buttons in CPU/GPU/I/O/TCP tabs to add settings.",
                    style = MaterialTheme.typography.bodySmall, color = GhostGray)

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        prefs.clear()
                        count = 0
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GhostRed.copy(alpha = 0.3f))
                ) {
                    Text("Clear All Boot Settings")
                }
            }
        }
    }
}
