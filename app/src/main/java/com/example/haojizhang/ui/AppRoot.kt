package com.example.haojizhang.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.ui.platform.LocalContext
import com.example.haojizhang.data.util.Prefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val ctx = LocalContext.current

    // ✅ 是否需要登录：启用PIN 且 PIN 非空
    val needLogin = remember {
        Prefs.isPinEnabled(ctx) && Prefs.getPin(ctx).isNotBlank()
    }

    val tabs = listOf(
        BottomTab("home", "首页", Icons.Filled.Home),
        BottomTab("bills", "账单", Icons.Filled.List),
        BottomTab("insights", "洞察", Icons.Filled.BarChart),
        BottomTab("profile", "我的", Icons.Filled.Person)
    )

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("豪记账") }) },
        floatingActionButton = {
            val route = currentRoute(navController)
            if (route != "login") {
                FloatingActionButton(onClick = { navController.navigate("add") }) {
                    Icon(Icons.Filled.Add, contentDescription = "新增账单")
                }
            }
        },
        bottomBar = {
            val route = currentRoute(navController)
            if (route != "login") {
                NavigationBar {
                    val currentRoute = currentRoute(navController)
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (needLogin) "login" else "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // ✅ 登录页（你需要自己有 LoginScreen.kt）
            composable("login") {
                LoginScreen(
                    onSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") { HomeScreen() }
            composable("bills") { BillsScreen() }
            composable("insights") { InsightsScreen() }
            composable("profile") { ProfileScreen() }
            composable("add") { AddBillScreen(navController) }
        }
    }
}

data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
