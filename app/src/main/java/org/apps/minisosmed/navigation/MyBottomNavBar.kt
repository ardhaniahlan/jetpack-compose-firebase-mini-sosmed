package org.apps.minisosmed.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MyBottomNavBar(navController: NavController){
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val barHeight = (screenHeight * 0.1f).coerceIn(56.dp, 80.dp)

    val items = listOf(
        Triple("home", Icons.Default.Home, "Beranda"),
        Triple("addpost", Icons.Default.Add, "Unggah"),
        Triple("search", Icons.Default.Search, "Cari"),
        Triple("profile", Icons.Default.AccountBox, "Profil")
    )

    NavigationBar(
        modifier = Modifier.height(barHeight)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { (route, icon, label) ->
                NavigationBarItem(
                    selected = currentDestination == route,
                    onClick = {
                        navController.navigate(route){
                            popUpTo(navController.graph.findStartDestination().id){
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(icon, contentDescription = null) },
                    label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.tertiary
                    ),
                    alwaysShowLabel = true
                )
            }
        }
    }
}