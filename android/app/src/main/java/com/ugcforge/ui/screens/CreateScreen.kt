package com.ugcforge.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ugcforge.core.AssetType
import com.ugcforge.core.QualityPreset
import com.ugcforge.ui.Routes
import com.ugcforge.ui.UgcForgeViewModel
import com.ugcforge.ui.theme.UgcColors

@Composable
fun CreateScreen(vm: UgcForgeViewModel, nav: NavHostController) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.HAIR) }
    var description by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("Roblox") }
    var primaryColor by remember { mutableStateOf("") }
    var secondaryColor by remember { mutableStateOf("") }
    var geometry by remember { mutableStateOf("stylized") }
    var quality by remember { mutableStateOf(QualityPreset.STANDARD) }
    var rightsConfirmed by remember { mutableStateOf(false) }
    val staged = remember { mutableStateListOf<String>() }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            val displayName = uri.lastPathSegment ?: "ref.jpg"
            vm.stageImage(uri, displayName)?.let { staged += it }
        }
    }

    DisposableEffect(Unit) {
        onDispose { vm.clearStaged() }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Create new asset", color = UgcColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Describe what you want forged. Reference images are optional; they help steer geometry.",
            color = UgcColors.TextSecondary,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Asset name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))

        Text("Asset type", color = UgcColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AssetType.entries.forEach { t ->
                FilterChip(
                    selected = type == t,
                    onClick = { type = t },
                    label = { Text(t.label, fontSize = 12.sp) },
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = style,
                onValueChange = { style = it },
                label = { Text("Style") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = geometry,
                onValueChange = { geometry = it },
                label = { Text("Geometry") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = primaryColor,
                onValueChange = { primaryColor = it },
                label = { Text("Primary color") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = secondaryColor,
                onValueChange = { secondaryColor = it },
                label = { Text("Secondary") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(14.dp))

        Text("Quality", color = UgcColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QualityPreset.entries.forEach { q ->
                FilterChip(
                    selected = quality == q,
                    onClick = { quality = q },
                    label = { Text(q.label, fontSize = 12.sp) },
                )
            }
        }
        Spacer(Modifier.height(18.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = UgcColors.Surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Reference images", color = UgcColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        if (staged.isEmpty()) "None staged" else "${staged.size} staged",
                        color = UgcColors.TextSecondary,
                        fontSize = 12.sp,
                    )
                }
                OutlinedButton(onClick = { picker.launch(arrayOf("image/*")) }) {
                    Text("Add", fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Switch(checked = rightsConfirmed, onCheckedChange = { rightsConfirmed = it })
            Spacer(Modifier.size(8.dp))
            Text(
                "I confirm I hold the rights to the content I am uploading.",
                color = UgcColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                if (name.isBlank()) return@Button
                vm.createProject(
                    name = name,
                    type = type,
                    description = description,
                    style = style,
                    primary = primaryColor,
                    secondary = secondaryColor,
                    geometry = geometry,
                    quality = quality,
                    stagedImages = staged.toList(),
                    rightsConfirmed = rightsConfirmed,
                ) { created ->
                    vm.refreshProjects()
                    nav.navigate(Routes.PROJECTS)
                }
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text("Queue asset", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
        }
        Spacer(Modifier.height(24.dp))
    }
}
