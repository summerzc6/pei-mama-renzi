package com.peimama.renzi.app

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.peimama.renzi.app.navigation.AppNavigation
import com.peimama.renzi.app.navigation.AppRoutes
import com.peimama.renzi.app.navigation.bottomNavItems
import com.peimama.renzi.di.AppContainer

@Composable
fun PeiMamaApp(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        AppRoutes.HOME,
        AppRoutes.NOTEBOOK,
        AppRoutes.FAMILY,
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(AppRoutes.HOME)
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            container = container,
            innerPadding = innerPadding,
        )
    }
}
