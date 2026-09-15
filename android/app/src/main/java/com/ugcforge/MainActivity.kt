package com.ugcforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.ugcforge.ui.UgcForgeApp
import com.ugcforge.ui.UgcForgeViewModel

class MainActivity : ComponentActivity() {

    private val vm: UgcForgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UgcForgeApp(vm)
        }
    }
}
