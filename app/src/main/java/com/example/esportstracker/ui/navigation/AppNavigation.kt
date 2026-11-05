package com.example.esportstracker.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.esportstracker.ui.auth.AuthViewModel
import com.example.esportstracker.ui.auth.LoginScreen
import com.example.esportstracker.ui.auth.RegisterScreen
import com.example.esportstracker.ui.auth.SessionState
import com.example.esportstracker.ui.favorites.FavoritesScreen
import com.example.esportstracker.ui.matches.MatchDetailScreen
import com.example.esportstracker.ui.matches.MatchListScreen
import com.example.esportstracker.ui.matches.TeamDetailScreen
import com.example.esportstracker.ui.predictions.PredictionsScreen
import com.example.esportstracker.ui.profile.ProfileScreen
import com.example.esportstracker.ui.reminders.RemindersScreen
import com.example.esportstracker.ui.theme.AppColors

@Composable
fun AppNavigation() {
    val authVm: AuthViewModel = viewModel()
    val session by authVm.session.collectAsState()

    when (session) {
        SessionState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        SessionState.LoggedOut -> AuthNavigation(authVm)
        is SessionState.LoggedIn -> MainNavigation(onLogout = { authVm.logout() })
    }
}

@Composable
private fun AuthNavigation(vm: AuthViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginScreen(vm, onGoRegister = { nav.navigate("register") })
        }
        composable("register") {
            RegisterScreen(vm, onGoLogin = { nav.popBackStack() })
        }
    }
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab("matches", "Trận", Icons.Outlined.SportsEsports),
    BottomTab("favorites", "Yêu thích", Icons.Outlined.Favorite),
    BottomTab("predictions", "Dự đoán", Icons.Outlined.Star),
    BottomTab("reminders", "Nhắc lịch", Icons.Outlined.Notifications),
    BottomTab("profile", "Hồ sơ", Icons.Outlined.Person)
)

@Composable
private fun MainNavigation(onLogout: () -> Unit) {
    val nav = rememberNavController()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBar = bottomTabs.any { it.route == currentRoute }

    Scaffold(
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBar) {
                Column {
                    HorizontalDivider(color = AppColors.Border)
                    NavigationBar(containerColor = AppColors.NavBar, tonalElevation = 0.dp) {
                        bottomTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    nav.navigate(tab.route) {
                                        popUpTo("matches") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AppColors.Primary,
                                    selectedTextColor = AppColors.Primary,
                                    indicatorColor = AppColors.PrimaryTint,
                                    unselectedIconColor = AppColors.TextMuted,
                                    unselectedTextColor = AppColors.TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = "matches",
            modifier = Modifier.padding(inner).consumeWindowInsets(inner)
        ) {
            composable("matches") {
                MatchListScreen(onMatchClick = { id -> nav.navigate("match/$id") })
            }
            composable("favorites") {
                FavoritesScreen(
                    onBack = { nav.popBackStack() },
                    onTeamClick = { id -> nav.navigate("team/$id") }
                )
            }
            composable("predictions") {
                PredictionsScreen(
                    onBack = { nav.popBackStack() },
                    onMatchClick = { id -> nav.navigate("match/$id") }
                )
            }
            composable("reminders") {
                RemindersScreen(
                    onBack = { nav.popBackStack() },
                    onMatchClick = { id -> nav.navigate("match/$id") }
                )
            }
            composable("profile") {
                ProfileScreen(onBack = { nav.popBackStack() }, onLogout = onLogout)
            }
            composable(
                route = "match/{matchId}",
                arguments = listOf(navArgument("matchId") { type = NavType.LongType })
            ) {
                MatchDetailScreen(
                    onBack = { nav.popBackStack() },
                    onTeamClick = { id -> nav.navigate("team/$id") }
                )
            }
            composable(
                route = "team/{teamId}",
                arguments = listOf(navArgument("teamId") { type = NavType.LongType })
            ) {
                TeamDetailScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}