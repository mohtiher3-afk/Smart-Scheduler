package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
    object Tasks : Screen("tasks")
    object Alerts : Screen("alerts")
    object More : Screen("more")
    object Courses : Screen("courses")
    object Calendar : Screen("calendar")
    object Analytics : Screen("analytics")
    object Notes : Screen("notes")
    object Files : Screen("files")
    object Exams : Screen("exams")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object SmartScheduler : Screen("smart_scheduler")
    object Calculator : Screen("calculator")
    object StudyHub : Screen("study_hub")
    object SyncCenter : Screen("sync_center")
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
            Screen.Tasks,
            Screen.Alerts,
            Screen.More
        )
    }

    val activeTab = when (currentRoute) {
        Screen.Dashboard.route -> 0
        Screen.Schedule.route -> 1
        Screen.Tasks.route -> 2
        Screen.Alerts.route -> 3
        Screen.More.route -> 4
        else -> 0
    }

    val tabs = remember(loc, lang) {
        listOf(
            NavigationTabItem(
                index = 0,
                label = if (lang == "ar") "الرئيسية" else "Home",
                icon = Icons.Rounded.Home,
                testTag = "tab_dashboard"
            ),
            NavigationTabItem(
                index = 1,
                label = if (lang == "ar") "الجدول" else "Schedule",
                icon = Icons.Rounded.CalendarMonth,
                testTag = "tab_schedule"
            ),
            NavigationTabItem(
                index = 2,
                label = if (lang == "ar") "المهام" else "Tasks",
                icon = Icons.Rounded.AddTask,
                testTag = "tab_tasks"
            ),
            NavigationTabItem(
                index = 3,
                label = if (lang == "ar") "التذكيرات" else "Alerts",
                icon = Icons.Rounded.Notifications,
                testTag = "tab_alerts"
            ),
            NavigationTabItem(
                index = 4,
                label = if (lang == "ar") "المزيد" else "More",
                icon = Icons.Rounded.GridView,
                testTag = "tab_more"
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
