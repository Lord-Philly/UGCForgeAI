package com.ugcforge.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import com.ugcforge.ui.screens.CreateScreen
import com.ugcforge.ui.screens.HomeScreen
import com.ugcforge.ui.screens.ProjectDetailScreen
import com.ugcforge.ui.screens.ProjectsScreen
import com.ugcforge.ui.screens.SettingsScreen
import com.ugcforge.ui.theme.UgcForgeTheme

object Routes {
    const val HOME = "home"
    const val CREATE = "create"
    const val PROJECTS = "projects"
    const val SETTINGS = "settings"
    fun project(id: String) = "project/$id"
}

@Composable
fun UgcForgeApp(vm: UgcForgeViewModel = viewModel()) {
    UgcForgeTheme {
        val nav = rememberNavController()
        val snackbarState = remember { SnackbarHostState() }
        val projects by vm.projects.collectAsState()
        val snackMessage by vm.snackbar.collectAsState()

        LaunchedEffect(snackMessage) {
            snackMessage?.let { message ->
                snackbarState.showSnackbar(message)
                vm.consumeSnackbar()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarState) },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                NavHost(navController = nav, startDestination = Routes.HOME) {
                    composable(Routes.HOME) { HomeScreen(vm, nav) }
                    composable(Routes.CREATE) { CreateScreen(vm, nav) }
                    composable(Routes.PROJECTS) { ProjectsScreen(vm, nav, projects) }
                    composable(Routes.SETTINGS) { SettingsScreen(vm) }
                    composable("project/{id}") { entry ->
                        val id = entry.arguments?.getString("id")
                        ProjectDetailScreen(vm, nav, projects, id)
                    }
                }
            }
        }
    }
}