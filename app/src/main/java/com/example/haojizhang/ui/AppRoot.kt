package com.example.haojizhang.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val navController = rememberNavController()

    val tabs = listOf(
        BottomTab("home", "首页", Icons.Filled.Home),
        BottomTab("bills", "账单", Icons.Filled.List),
        BottomTab("insights", "报表", Icons.Filled.BarChart),
        BottomTab("profile", "我的", Icons.Filled.Person)
    )

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("豪记账") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add?type=0") }) {
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
            composable("home") {
                HomeScreen(
                    onAddExpense = { navController.navigate("add?type=0") },
                    onAddIncome = { navController.navigate("add?type=1") }
                )
            }
            composable("bills") { BillsScreen() }
            composable("insights") { InsightsScreen() }
            composable("profile") { ProfileScreen() }

            composable(
                route = "add?type={type}",
                arguments = listOf(
                    navArgument("type") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getInt("type") ?: 0
                AddBillScreen(
                    initialType = type,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

data class BottomTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun currentRoute(navController: androidx.navigation.NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route?.substringBefore("?")
}
