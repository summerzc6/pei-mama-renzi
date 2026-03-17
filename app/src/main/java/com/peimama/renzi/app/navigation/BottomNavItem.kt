package com.peimama.renzi.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = AppRoutes.HOME,
        label = "首页",
        icon = Icons.Filled.Home,
    ),
    BottomNavItem(
        route = AppRoutes.NOTEBOOK,
        label = "我的字本",
        icon = Icons.Filled.MenuBook,
    ),
    BottomNavItem(
        route = AppRoutes.FAMILY,
        label = "家属陪学",
        icon = Icons.Filled.Favorite,
    ),
)
