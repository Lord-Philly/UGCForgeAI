package com.ugcforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ugcforge.core.ProjectStatus
import com.ugcforge.ui.theme.UgcColors

@Composable
fun StatusChip(status: ProjectStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        ProjectStatus.COMPLETED -> Color(0xFF1B5E20) to Color(0xFFA5D6A7)
        ProjectStatus.QUEUED -> Color(0xFF263238) to Color(0xFFB0BEC5)
        ProjectStatus.PROCESSING,
        ProjectStatus.OPTIMIZING,
        ProjectStatus.VALIDATING -> Color(0xFF1A3A5C) to Color(0xFF81D4FA)
        ProjectStatus.NEEDS_BACKEND -> Color(0xFF4E342E) to Color(0xFFFFCC80)
        ProjectStatus.FAILED -> Color(0xFF4A1A1A) to Color(0xFFFFABAB)
    }
    Text(
        text = status.label,
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun MonogramBadge(monogram: String, color: Long, modifier: Modifier = Modifier, size: Int = 52) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(Color(color), CircleShape)
            .padding(size.dp),
    ) {
        Text(
            text = monogram,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.5f).sp,
        )
    }
}