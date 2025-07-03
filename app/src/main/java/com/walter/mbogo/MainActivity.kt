package com.walter.mbogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.walter.mbogo.nav.Destinations
import com.walter.mbogo.nav.bottomNavItems
import com.walter.mbogo.screens.ChartsScreen
import com.walter.mbogo.screens.ExpensesScreen
import com.walter.mbogo.screens.IncomeScreen
import com.walter.mbogo.ui.theme.MbogoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MbogoTheme {
                NavigationBarMotherScreen()
            }
        }
    }
}


@Composable
fun NavigationBarMotherScreen() { // Renamed from your original example for clarity
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Income.route, // Your starting screen
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.Income.route) {
                IncomeScreen()
            }
            composable(Destinations.Expenses.route) {
                ExpensesScreen()
            }
            composable(Destinations.Graphs.route) {
                ChartsScreen()
            }
        }
    }
}






@Composable
fun AppBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                alwaysShowLabel = true // Or false, depending on your design preference
            )
        }
    }
}
