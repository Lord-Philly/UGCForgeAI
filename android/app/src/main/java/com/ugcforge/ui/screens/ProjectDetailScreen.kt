package com.ugcforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.navigation.NavHostController
import com.ugcforge.core.UgcProject
import com.ugcforge.ui.Routes
import com.ugcforge.ui.UgcForgeViewModel
import com.ugcforge.ui.components.MonogramBadge
import com.ugcforge.ui.components.StatusChip
import com.ugcforge.ui.theme.UgcColors

@Composable
fun ProjectDetailScreen(vm: UgcForgeViewModel, nav: NavHostController, projects: List<UgcProject>, id: String?) {
    val project = projects.firstOrNull { it.id == id }
    if (project == null) {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
            Text("Project not found", color = UgcColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Go back")
            }
        }
        return
    }

    var phaseLabel by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        androidx.compose.material3.IconButton(onClick = { nav.popBackStack() }) {
            androidx.compose.material3.Icon(
                androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = UgcColors.TextPrimary,
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            MonogramBadge(monogram = project.assetType.monogram, color = project.assetType.color, size = 54)
            Spacer(Modifier.padding(start = 12.dp))
            Column(Modifier.weight(1f)) {
                Text(project.name, color = UgcColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(2.dp))
                Text(project.assetType.label + " · " + project.assetType.robloxProfile, color = UgcColors.TextSecondary, fontSize = 12.sp)
            }
            StatusChip(project.status)
        }
        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = UgcColors.Surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, UgcColors.Border),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        ) {
            Column(Modifier.padding(16.dp)) {
                DetailRow("Description", project.description)
                DetailRow("Style", project.style)
                DetailRow("Geometry", project.geometryStyle)
                DetailRow("Quality", project.qualityPreset.label)
            }
        }
        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                phaseLabel = "Starting…"
                vm.requestGeneration(project, onPhase = { phase -> phaseLabel = phase.label })
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Generate mesh", fontWeight = FontWeight.Bold)
        }
        phaseLabel?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = UgcColors.TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { vm.duplicateProject(project.id); nav.navigate(Routes.PROJECTS) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Duplicate") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { vm.deleteProject(project.id); nav.navigate(Routes.PROJECTS) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Delete", color = UgcColors.Error) }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = UgcColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.35f))
        Text(value.ifBlank { "—" }, color = UgcColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.65f))
    }
}
