package com.ghostkernel.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghostkernel.manager.ui.theme.*
import com.ghostkernel.manager.viewmodel.IoDevice
import com.ghostkernel.manager.viewmodel.IoViewModel

@Composable
fun IoScreen(vm: IoViewModel) {
    val devices by vm.devices.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("I/O Scheduler", style = MaterialTheme.typography.headlineMedium, color = GhostCyan)
        Spacer(Modifier.height(16.dp))
        devices.forEach { dev ->
            IoDeviceCard(dev, vm)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun IoDeviceCard(dev: IoDevice, vm: IoViewModel) {
    ContentCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(dev.name, style = MaterialTheme.typography.titleMedium, color = GhostCyan)

            // Scheduler
            var schedExpanded by remember { mutableStateOf(false) }
            Text("Scheduler", style = MaterialTheme.typography.bodySmall, color = GhostGray)
            Box {
                OutlinedButton(onClick = { schedExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)) {
                    Text(dev.scheduler, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(expanded = schedExpanded, onDismissRequest = { schedExpanded = false }) {
                    dev.availableSchedulers.forEach { sched ->
                        DropdownMenuItem(
                            text = { Text(sched, style = MaterialTheme.typography.bodyMedium) },
                            onClick = { schedExpanded = false; vm.setScheduler(dev.name, sched) }
                        )
                    }
                }
            }

            // Read Ahead
            Text("Read-ahead: ${dev.readAheadKb} KB",
                style = MaterialTheme.typography.bodyMedium, color = GhostAmber)
        }
    }
}
