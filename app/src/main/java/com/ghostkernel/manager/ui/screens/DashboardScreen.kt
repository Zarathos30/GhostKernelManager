package com.ghostkernel.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ghostkernel.manager.data.BootPrefs
import com.ghostkernel.manager.data.KernelDetector
import com.ghostkernel.manager.ui.theme.*
import com.ghostkernel.manager.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(vm: DashboardViewModel) {
    val info by vm.kernelInfo.collectAsState()
    val isGhost by vm.isGhostKernel.collectAsState()
    val root by vm.rootAvailable.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        if (!isGhost) {
            ContentCard {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("GhostKernel NOT detected", style = MaterialTheme.typography.titleMedium, color = GhostRed)
                    Spacer(Modifier.height(8.dp))
                    Text("This app only works with GhostKernel.\nFlash GhostKernel to use this manager.", style = MaterialTheme.typography.bodyMedium, color = GhostGray)
                }
            }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ContentCard {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("GhostKernel", style = MaterialTheme.typography.headlineMedium, color = GhostCyan)
                            Spacer(Modifier.weight(1f))
                            Surface(shape = MaterialTheme.shapes.small, color = if (root) GhostGreen.copy(alpha = 0.15f) else GhostRed.copy(alpha = 0.15f)) {
                                Text(if (root) " ROOT " else " NO ROOT ",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (root) GhostGreen else GhostRed)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("v${info.kernelVersion}", style = MaterialTheme.typography.bodyMedium, color = GhostGray)
                    }
                }

                Spacer(Modifier.height(12.dp))
                ContentCard {
                    Column(Modifier.padding(20.dp)) {
                        InfoRow("Kernel", info.kernelVersion)
                        InfoRow("Local Version", info.localVersion)
                        InfoRow("Architecture", info.cpuArch)
                        InfoRow("CPU Cores", info.numCpus.toString())
                        InfoRow("SoC", info.soc)
                        InfoRow("Compiler", info.compiler)
                        InfoRow("Host", info.hostname)
                        InfoRow("Uptime", info.uptime)
                    }
                }

                Spacer(Modifier.height(16.dp))
                ContentCard {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("@Zarathos30", style = MaterialTheme.typography.bodyMedium, color = GhostGray)
                        Text("GhostKernel Manager v1.0.0", style = MaterialTheme.typography.bodySmall, color = GhostGray)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = GhostGray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = GhostWhite)
    }
    HorizontalDivider(color = GhostCardBg, thickness = 0.5.dp)
}
