package com.ghostkernel.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
        Text("GPU Info", style = MaterialTheme.typography.headlineMedium, color = GhostCyan)
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
                else "${freq / 1_000_000} MHz"
            }

            ContentCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Adreno GPU", style = MaterialTheme.typography.titleMedium, color = GhostCyan)
                    InfoRow("Governor", info.governor)
                    InfoRow("Current Freq", freqText(info.curFreq))
                    InfoRow("Min Freq", freqText(info.minFreq))
                    InfoRow("Max Freq", freqText(info.maxFreq))
                }
            }

            if (info.availableFrequencies.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                ContentCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Available Frequencies", style = MaterialTheme.typography.titleSmall, color = GhostCyan)
                        Spacer(Modifier.height(8.dp))
                        info.availableFrequencies.chunked(3).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { freq ->
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = GhostCardBg,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            freqText(freq),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GhostAmber
                                        )
                                    }
                                }
                                repeat(3 - row.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}
