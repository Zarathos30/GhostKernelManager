package com.ghostkernel.manager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ghostkernel.manager.ui.theme.*
import com.ghostkernel.manager.viewmodel.CpuCluster
import com.ghostkernel.manager.viewmodel.CpuViewModel

@Composable
fun CpuScreen(vm: CpuViewModel) {
    val clusters by vm.clusters.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("CPU Control", style = MaterialTheme.typography.headlineMedium, color = GhostCyan)
        Spacer(Modifier.height(16.dp))
        clusters.forEach { cluster ->
            ClusterCard(cluster, vm)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun ClusterCard(cluster: CpuCluster, vm: CpuViewModel) {
    val leaderCpu = cluster.cpus.first()
    val freqText = { freq: Long ->
        if (freq >= 1_000_000) "${"%.2f".format(freq / 1_000_000f)} GHz"
        else "${freq / 1000} MHz"
    }

    ContentCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Cluster ${cluster.index + 1}",
                style = MaterialTheme.typography.titleMedium, color = GhostCyan)
            Text("CPUs: ${cluster.cpus.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall, color = GhostGray)

            // Governor
            var govExpanded by remember { mutableStateOf(false) }
            Text("Governor", style = MaterialTheme.typography.bodySmall, color = GhostGray)
            Box {
                OutlinedButton(onClick = { govExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)) {
                    Text(cluster.governor, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(expanded = govExpanded, onDismissRequest = { govExpanded = false }) {
                    cluster.availableGovernors.forEach { gov ->
                        DropdownMenuItem(
                            text = { Text(gov, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                govExpanded = false
                                vm.setGovernor(leaderCpu, gov)
                            }
                        )
                    }
                }
            }

            // Current Frequency
            Text("Current: ${freqText(cluster.curFreq)}",
                style = MaterialTheme.typography.bodyMedium, color = GhostAmber)

            // Min Freq
            if (cluster.availableFrequencies.isNotEmpty()) {
                var minExpanded by remember { mutableStateOf(false) }
                Text("Min Frequency", style = MaterialTheme.typography.bodySmall, color = GhostGray)
                Box {
                    OutlinedButton(onClick = { minExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)) {
                        Text(freqText(cluster.minFreq), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = minExpanded, onDismissRequest = { minExpanded = false }) {
                        cluster.availableFrequencies.forEach { f ->
                            DropdownMenuItem(
                                text = { Text(freqText(f), style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    minExpanded = false
                                    vm.setMinFreq(leaderCpu, f)
                                }
                            )
                        }
                    }
                }

                // Max Freq
                var maxExpanded by remember { mutableStateOf(false) }
                Text("Max Frequency", style = MaterialTheme.typography.bodySmall, color = GhostGray)
                Box {
                    OutlinedButton(onClick = { maxExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)) {
                        Text(freqText(cluster.maxFreq), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = maxExpanded, onDismissRequest = { maxExpanded = false }) {
                        cluster.availableFrequencies.forEach { f ->
                            DropdownMenuItem(
                                text = { Text(freqText(f), style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    maxExpanded = false
                                    vm.setMaxFreq(leaderCpu, f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
