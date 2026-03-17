package com.peimama.renzi.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.peimama.renzi.di.AppContainer

@Composable
fun AppNavigation(
    navController: NavHostController,
    container: AppContainer,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    AppNavHost(
        navController = navController,
        container = container,
        innerPadding = innerPadding,
        modifier = modifier,
    )
}
