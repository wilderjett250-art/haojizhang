package com.example.haojizhang.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val navController = rememberNavController()

    val tabs = listOf(
        BottomTab("home", "首页", Icons.Filled.Home),
        BottomTab("bills", "账单", Icons.Filled.List),
        BottomTab("insights", "洞察", Icons.Filled.BarChart),
        BottomTab("profile", "我的", Icons.Filled.Person)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("豪记账") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add") }) {
                Icon(Icons.Filled.Add, contentDescription = "新增")
            }
        },
        bottomBar = {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("bills") { BillsScreen() }
            composable("insights") { InsightsScreen() }
            composable("profile") { ProfileScreen() }
            composable("add") { AddBillScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

data class BottomTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
