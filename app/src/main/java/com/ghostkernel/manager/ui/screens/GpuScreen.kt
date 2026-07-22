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
import com.ghostkernel.manager.viewmodel.GpuViewModel

@Composable
fun GpuScreen(vm: GpuViewModel) {
    val gpu by vm.gpu.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("GPU Control", style = MaterialTheme.typography.headlineMedium, color = GhostCyan)
        Spacer(Modifier.height(16.dp))

        val info = gpu
        if (info == null) {
            ContentCard {
                Column(Modifier.padding(24.dp)) {
                    Text("No GPU sysfs detected", style = MaterialTheme.typography.bodyMedium, color = GhostGray)
                    Spacer(Modifier.height(4.dp))
                    Text("Your device may not expose GPU controls via sysfs.", style = MaterialTheme.typography.bodySmall, color = GhostGray)
                }
            }
        } else {
            val freqText = { freq: Long ->
                if (freq >= 1_000_000_000) "${"%.2f".format(freq / 1_000_000_000f)} GHz"
                else if (freq >= 1_000_000) "${freq / 1_000_000} MHz"
                else freq.toString()
            }

            ContentCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(info.model, style = MaterialTheme.typography.titleMedium, color = GhostCyan)

                    // Governor
                    if (info.availableGovernors.isNotEmpty()) {
                        var govExpanded by remember { mutableStateOf(false) }
                        Text("Governor", style = MaterialTheme.typography.bodySmall, color = GhostGray)
                        Box {
                            OutlinedButton(onClick = { govExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)) {
                                Text(info.governor, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = govExpanded, onDismissRequest = { govExpanded = false }) {
                                info.availableGovernors.forEach { gov ->
                                    DropdownMenuItem(
                                        text = { Text(gov, style = MaterialTheme.typography.bodyMedium) },
                                        onClick = { govExpanded = false; vm.setGovernor(gov) }
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Governor: ${info.governor}", style = MaterialTheme.typography.bodyMedium, color = GhostWhite)
                    }

                    // Frequencies
                    if (info.curFreq > 0) {
                        Text("Current: ${freqText(info.curFreq)}", style = MaterialTheme.typography.bodyMedium, color = GhostAmber)
                    }

                    if (info.availableFrequencies.isNotEmpty()) {
                        var minExpanded by remember { mutableStateOf(false) }
                        Text("Min Frequency", style = MaterialTheme.typography.bodySmall, color = GhostGray)
                        Box {
                            OutlinedButton(onClick = { minExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)) {
                                Text(freqText(info.minFreq), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = minExpanded, onDismissRequest = { minExpanded = false }) {
                                info.availableFrequencies.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text(freqText(f), style = MaterialTheme.typography.bodyMedium) },
                                        onClick = { minExpanded = false; vm.setMinFreq(f) }
                                    )
                                }
                            }
                        }

                        var maxExpanded by remember { mutableStateOf(false) }
                        Text("Max Frequency", style = MaterialTheme.typography.bodySmall, color = GhostGray)
                        Box {
                            OutlinedButton(onClick = { maxExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)) {
                                Text(freqText(info.maxFreq), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = maxExpanded, onDismissRequest = { maxExpanded = false }) {
                                info.availableFrequencies.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text(freqText(f), style = MaterialTheme.typography.bodyMedium) },
                                        onClick = { maxExpanded = false; vm.setMaxFreq(f) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
