package com.dergoogler.mmrl.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dergoogler.mmrl.Compat
import com.dergoogler.mmrl.datastore.UserPreferencesCompat.Companion.isRoot
import com.dergoogler.mmrl.ui.component.TopAppBarIcon
import com.dergoogler.mmrl.ui.navigation.MainScreen
import com.dergoogler.mmrl.ui.navigation.graphs.homeScreen
import com.dergoogler.mmrl.ui.navigation.graphs.modulesScreen
import com.dergoogler.mmrl.ui.navigation.graphs.repositoryScreen
import com.dergoogler.mmrl.ui.navigation.graphs.settingsScreen
import com.dergoogler.mmrl.ui.navigation.graphs.superUserScreen
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.providable.LocalWindowWidthSizeClass
import com.dergoogler.mmrl.ui.providable.WindowWidthSize
import com.dergoogler.mmrl.ui.utils.bars
import com.dergoogler.mmrl.ui.utils.barsWithSystem
import com.dergoogler.mmrl.ui.utils.navigatePopUpTo
import com.dergoogler.mmrl.ui.utils.none
import com.dergoogler.mmrl.viewmodel.BulkInstallViewModel
import dev.dergoogler.mmrl.compat.impl.ksu.KsuNative

@Composable
fun MainScreen(windowSizeClass: WindowSizeClass) {
    val context = LocalContext.current
    val userPreferences = LocalUserPreferences.current
    val bulkInstallViewModel: BulkInstallViewModel = hiltViewModel()

    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current

    val windowSize = WindowWidthSize(configuration, windowSizeClass)
    val isRoot = userPreferences.workingMode.isRoot

    val platform = Compat.platform
    val isKsuManager = KsuNative.becomeManager(context.packageName)

    val mainScreens by remember(platform, isRoot) {
        derivedStateOf {
            if (platform.isKernelSuOrNext && isKsuManager && isRoot) {
                return@derivedStateOf listOf(
                    MainScreen.Home,
                    MainScreen.SuperUser,
                    MainScreen.Repository,
                    MainScreen.Modules,
                    MainScreen.Settings
                )
            }

            if (isRoot) {
                return@derivedStateOf listOf(
                    MainScreen.Home,
                    MainScreen.Repository,
                    MainScreen.Modules,
                    MainScreen.Settings
                )
            }

            return@derivedStateOf listOf(
                MainScreen.Home,
                MainScreen.Repository,
                MainScreen.Settings
            )
        }
    }

    Scaffold(
        bottomBar = {
            if (windowSize.isRailShown) return@Scaffold

            BottomNav(mainScreens)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.none
    ) { paddingValues ->
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHostState,
            LocalWindowWidthSizeClass provides windowSize
        ) {
            Row {
                if (windowSize.isRailShown) RailNav(mainScreens)

                NavHost(
                    modifier = Modifier.let {
                        if (windowSize.isRailShown) {
                            return@let Modifier
                        }

                        return@let it.padding(bottom = paddingValues.calculateBottomPadding())
                    },
                    navController = navController,
                    startDestination = when (userPreferences.homepage) {
                        context.getString(MainScreen.Home.label) -> MainScreen.Home.route
                        context.getString(MainScreen.Repository.label) -> MainScreen.Repository.route
                        context.getString(MainScreen.Modules.label) -> MainScreen.Modules.route
                        else -> MainScreen.Home.route
                    }
                ) {
                    homeScreen()
                    superUserScreen()
                    repositoryScreen(bulkInstallViewModel = bulkInstallViewModel)
                    modulesScreen()
                    settingsScreen()
                }
            }
        }
    }
}

@Composable
private fun BottomNav(
    mainScreens: List<MainScreen>,
) {
    val navController = LocalNavController.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
            val selected =
                currentDestination?.hierarchy?.any { it.route == screen.route } == true

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
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    navController.navigatePopUpTo(
                        route = screen.route,
                        restoreState = !selected
                    )
                }
            )
        }
    }
}

@Composable
private fun RailNav(
    mainScreens: List<MainScreen>,
) {
    val navController = LocalNavController.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val layoutDirection = LocalLayoutDirection.current

    NavigationRail(
        header = {
            TopAppBarIcon()
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .background(NavigationRailDefaults.ContainerColor)
            .padding(
                start = WindowInsets.bars
                    .asPaddingValues()
                    .calculateStartPadding(layoutDirection),
                top = WindowInsets.barsWithSystem
                    .asPaddingValues()
                    .calculateTopPadding(),
            ),
        windowInsets = WindowInsets.none,
        content = {
            mainScreens.forEach { screen ->
                val selected =
                    currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationRailItem(
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
                    alwaysShowLabel = true,
                    selected = selected,
                    onClick = {
                        navController.navigatePopUpTo(
                            route = screen.route,
                            restoreState = !selected
                        )
                    }
                )
            }
        }
    )
}


