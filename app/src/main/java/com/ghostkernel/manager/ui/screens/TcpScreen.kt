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
import com.ghostkernel.manager.viewmodel.TcpViewModel

@Composable
fun TcpScreen(vm: TcpViewModel) {
    val current by vm.current.collectAsState()
    val available by vm.available.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("TCP Congestion", style = MaterialTheme.typography.headlineMedium, color = GhostCyan)
        Spacer(Modifier.height(16.dp))

        ContentCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Current: $current", style = MaterialTheme.typography.bodyMedium, color = GhostAmber)

                var expanded by remember { mutableStateOf(false) }
                Text("Select Algorithm", style = MaterialTheme.typography.bodySmall, color = GhostGray)
                Box {
                    OutlinedButton(onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)) {
                        Text(current, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        available.forEach { alg ->
                            DropdownMenuItem(
                                text = { Text(alg, style = MaterialTheme.typography.bodyMedium) },
                                onClick = { expanded = false; vm.setAlgorithm(alg) }
                            )
                        }
                    }
                }
            }
        }
    }
}
