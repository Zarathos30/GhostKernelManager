package com.ghostkernel.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghostkernel.manager.ui.theme.GhostCardBg
import com.ghostkernel.manager.ui.theme.GhostCyanDim

@Composable
fun ContentCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GhostCardBg,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .background(GhostCardBg)
    ) {
        content()
    }
}
