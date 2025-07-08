package com.walter.mbogo.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllOut
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.ui.graphics.vector.ImageVector


sealed interface Destinations {
    val route: String
    val title: String
    val icon: ImageVector

    data object Expenses : Destinations {
        override val route: String = "expenses"
        override val title: String = "Expenses"
        override val icon: ImageVector = Icons.Filled.Money
    }

    data object Income : Destinations {
        override val route: String = "income"
        override val title: String = "Income"
        override val icon: ImageVector = Icons.Filled.Payments
    }

    data object Graphs : Destinations {
        override val route: String = "graphs"
        override val title: String = "Graphs"
        override val icon: ImageVector = Icons.Filled.AllOut
    }
}

// Helper list for easy iteration in the BottomNavigationBar
val bottomNavItems = listOf(
    Destinations.Expenses,
    Destinations.Income,
    Destinations.Graphs
)