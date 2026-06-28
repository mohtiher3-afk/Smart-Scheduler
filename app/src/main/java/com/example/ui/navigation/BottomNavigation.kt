package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.screens.Loc
import com.example.screens.LocalAppLanguage
import com.example.ui.components.NavigationTabItem
import com.example.ui.components.SmartBottomBar

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Schedule : Screen("schedule")
    object SmartScheduler : Screen("smart_scheduler")
    object Calculator : Screen("calculator")
    object Alerts : Screen("alerts")
}

@Composable
fun BottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val loc = remember(lang) { Loc(lang) }

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val screens = remember {
        listOf(
            Screen.Dashboard,
            Screen.Schedule,
            Screen.SmartScheduler,
            Screen.Calculator,
            Screen.Alerts
        )
    }

    val activeTab = when (currentRoute) {
        Screen.Dashboard.route -> 0
        Screen.Schedule.route -> 1
        Screen.SmartScheduler.route -> 2
        Screen.Calculator.route -> 3
        Screen.Alerts.route -> 4
        else -> 0
    }

    val tabs = remember(loc) {
        listOf(
            NavigationTabItem(
                index = 0,
                label = loc.tabDashboard,
                icon = Icons.Rounded.PieChart,
                testTag = "tab_dashboard"
            ),
            NavigationTabItem(
                index = 1,
                label = loc.tabSchedule,
                icon = Icons.Rounded.List,
                testTag = "tab_schedule"
            ),
            NavigationTabItem(
                index = 2,
                label = loc.tabSmartScheduler,
                icon = Icons.Rounded.AutoAwesome,
                testTag = "tab_smart_scheduler"
            ),
            NavigationTabItem(
                index = 3,
                label = loc.tabCalculator,
                icon = Icons.Rounded.Calculate,
                testTag = "tab_calculator"
            ),
            NavigationTabItem(
                index = 4,
                label = loc.tabAlerts,
                icon = Icons.Rounded.Notifications,
                testTag = "tab_alerts"
            )
        )
    }

    SmartBottomBar(
        tabs = tabs,
        activeTab = activeTab,
        onTabSelected = { index ->
            val targetRoute = screens[index].route
            if (currentRoute != targetRoute) {
                navController.navigate(targetRoute) {
                    popUpTo(Screen.Dashboard.route) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        modifier = modifier
    )
}
