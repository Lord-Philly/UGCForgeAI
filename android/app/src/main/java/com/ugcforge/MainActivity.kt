package com.ugcforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ugcforge.ui.UgcForgeApp
import com.ugcforge.ui.UgcForgeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: UgcForgeViewModel = viewModel()
            UgcForgeApp(vm)
        }
    }
}