package com.ugcforge.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ugcforge.ui.Routes
import com.ugcforge.ui.UgcForgeViewModel
import com.ugcforge.ui.theme.UgcColors

@Composable
fun HomeScreen(vm: UgcForgeViewModel, nav: NavHostController) {
    val projectCount = vm.projects.value.size
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text("UGC FORGE", color = UgcColors.Primary, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, fontSize = 13.sp)
        Text("AI", color = UgcColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 40.sp)
        Text(
            "Creator tool for Roblox assets",
            color = UgcColors.TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 2.dp),
        )

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF2A1A5E), Color(0xFF7C4DFF)))),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "CREATE ASSET",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp,
                )
                Text(
                    "Image or description → mesh → validate → export",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                )
                Button(
                    onClick = { nav.navigate(Routes.CREATE) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2A1A5E),
                    ),
                    shape = RoundedCornerShape(50),
                ) {
                    Text("Start", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            HomeCard(
                title = "Projects",
                subtitle = "$projectCount saved",
                color = UgcColors.Accent,
                onClick = { nav.navigate(Routes.PROJECTS) },
                modifier = Modifier.weight(1f),
            )
            HomeCard(
                title = "Settings",
                subtitle = "Backend & privacy",
                color = UgcColors.Secondary,
                onClick = { nav.navigate(Routes.SETTINGS) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        HomeCard(
            title = "Asset Library",
            subtitle = "Samples, presets and templates (soon)",
            color = UgcColors.Primary,
            onClick = { /* library landing in next iteration */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
        )
    }
}

@Composable
private fun HomeCard(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = UgcColors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, UgcColors.Border),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
            Spacer(Modifier.height(12.dp))
            Text(title, color = UgcColors.TextPrimary.copy(alpha = if (enabled) 1f else 0.45f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = UgcColors.TextSecondary.copy(alpha = if (enabled) 1f else 0.5f), fontSize = 12.sp)
        }
    }
}