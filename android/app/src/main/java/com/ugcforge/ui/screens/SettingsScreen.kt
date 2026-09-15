package com.ugcforge.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ugcforge.data.PrivacyMode
import com.ugcforge.ui.UgcForgeViewModel
import com.ugcforge.ui.theme.UgcColors

@Composable
fun SettingsScreen(vm: UgcForgeViewModel) {
    val settings = vm.settings
    var backendUrl by remember { mutableStateOf(settings.backendUrl) }
    var local by remember { mutableStateOf(settings.useLocalEngine) }
    var privacy by remember { mutableStateOf(settings.privacyMode) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("SETTINGS", color = UgcColors.TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 1.sp)
        Text("Backend & privacy", color = UgcColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(Modifier.height(18.dp))

        Text("AI backend URL", color = UgcColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = backendUrl,
            onValueChange = { backendUrl = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
        )
        Spacer(Modifier.height(14.dp))

        Text("Privacy", color = UgcColors.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            PrivacyMode.entries.forEach { mode ->
                androidx.compose.material3.FilterChip(
                    selected = privacy == mode,
                    onClick = { privacy = mode },
                    label = { Text(mode.label, fontSize = 11.sp) },
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Use local engine", color = UgcColors.TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("Process references on device (offline-first).", color = UgcColors.TextSecondary, fontSize = 11.sp)
            }
            Switch(checked = local, onCheckedChange = { local = it })
        }
        Spacer(Modifier.height(22.dp))

        androidx.compose.material3.Button(
            onClick = {
                settings.backendUrl = backendUrl
                settings.privacyMode = privacy
                settings.useLocalEngine = local
                vm.refreshProjects()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save settings", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { }, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("AI provider: local")
        }
        Spacer(Modifier.height(24.dp))
        Text("Part of Lord-Philly's UGC Forge AI.", color = UgcColors.TextSecondary, fontSize = 11.sp)
    }
}
