package com.ugcforge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ugcforge.core.UgcProject
import com.ugcforge.ui.Routes
import com.ugcforge.ui.UgcForgeViewModel
import com.ugcforge.ui.components.MonogramBadge
import com.ugcforge.ui.components.StatusChip
import com.ugcforge.ui.theme.UgcColors

@Composable
fun ProjectsScreen(vm: UgcForgeViewModel, nav: NavHostController, projects: List<UgcProject>) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("PROJECTS", color = UgcColors.TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.sp)
        Text("Saved projects", color = UgcColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(Modifier.height(16.dp))

        if (projects.isEmpty()) {
            Spacer(Modifier.height(40.dp))
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No projects yet", color = UgcColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text("Create your first asset to see it here.", color = UgcColors.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { nav.navigate(Routes.CREATE) }) {
                    Text("New asset")
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(projects, key = { it.id }) { project ->
                    Card(
                        onClick = { nav.navigate(Routes.project(project.id)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = UgcColors.Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, UgcColors.Border),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            MonogramBadge(
                                monogram = project.assetType.monogram,
                                color = project.assetType.color,
                                size = 42,
                            )
                            Spacer(Modifier.padding(start = 12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(project.name, color = UgcColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(project.assetType.label, color = UgcColors.TextSecondary, fontSize = 12.sp)
                            }
                            StatusChip(project.status)
                        }
                    }
                }
            }
        }
    }
}
