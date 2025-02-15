package com.dergoogler.mmrl.ui.activity

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dergoogler.mmrl.datastore.UserPreferencesCompat.Companion.isRoot
import com.dergoogler.mmrl.ui.navigation.MainScreen
import com.dergoogler.mmrl.ui.navigation.graphs.homeScreen
import com.dergoogler.mmrl.ui.navigation.graphs.modulesScreen
import com.dergoogler.mmrl.ui.navigation.graphs.repositoryScreen
import com.dergoogler.mmrl.ui.navigation.graphs.settingsScreen
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.utils.navigatePopUpTo
import com.dergoogler.mmrl.viewmodel.BulkInstallViewModel

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val userPreferences = LocalUserPreferences.current
    val bulkInstallViewModel: BulkInstallViewModel = hiltViewModel()

    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = {
            BottomNav(
                navController = navController,
                isRoot = userPreferences.workingMode.isRoot
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHostState
        ) {
            NavHost(
                modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
                navController = navController,
                startDestination = when (userPreferences.homepage) {
                    context.getString(MainScreen.Home.label) -> MainScreen.Home.route
                    context.getString(MainScreen.Repository.label) -> MainScreen.Repository.route
                    context.getString(MainScreen.Modules.label) -> MainScreen.Modules.route
                    else -> MainScreen.Home.route
                }
            ) {
                homeScreen()
                repositoryScreen(
                    bulkInstallViewModel = bulkInstallViewModel
                )
                modulesScreen()
                settingsScreen()
            }
        }
    }
}

@Composable
private fun BottomNav(
    navController: NavController,
    isRoot: Boolean,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val mainScreens by remember(isRoot) {
        derivedStateOf {
            if (isRoot) {
                listOf(
                    MainScreen.Home,
                    MainScreen.Repository,
                    MainScreen.Modules,
                    MainScreen.Settings
                )
            } else {
                listOf(MainScreen.Home, MainScreen.Repository, MainScreen.Settings)
            }
        }
    }

    NavigationBar(
        modifier = Modifier
            .imePadding()
            .clip(
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                )
            )
    ) {
        mainScreens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selected) {
                                screen.iconFilled
                            } else {
                                screen.icon
                            }
                        ),
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = screen.label),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                alwaysShowLabel = true, selected = selected, onClick = {
                    navController.navigatePopUpTo(
                        route = screen.route,
                        restoreState = !selected
                    )
                }
            )
        }
    }
}