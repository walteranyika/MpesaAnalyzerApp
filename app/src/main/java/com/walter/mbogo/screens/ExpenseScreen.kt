package com.walter.mbogo.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.walter.mbogo.db.MoneyItem
import com.walter.mbogo.utility.formatCurrency
import com.walter.mbogo.viewmodels.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(transactionViewModel: TransactionViewModel = viewModel()) {
    val allExpenseItems by transactionViewModel.allExpenses.observeAsState(initial = emptyList())
    var searchText by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) } // To control SearchBar expanded state
    var filteredExpenses by remember { mutableStateOf<List<MoneyItem>>(emptyList()) }

    // Filter logic
    LaunchedEffect(searchText, allExpenseItems) {
        filteredExpenses = if (searchText.isBlank()) {
            allExpenseItems // Show all if search is blank
        } else {
            allExpenseItems.filter { expense ->
                expense.person?.contains(searchText, ignoreCase = true) == true ||
                        expense.amount.toString().contains(searchText, ignoreCase = true) ||
                        expense.phone?.contains(searchText, ignoreCase = true) == true ||
                        // Add more fields to search if needed (e.g., date, code)
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Date(expense.date)).contains(searchText, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // We use a Box to allow the SearchBar to expand and overlay content
            // when active, or be part of the layout flow when inactive.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Add padding for the status bar
                    .padding(horizontal = if (isActive) 0.dp else 16.dp, vertical = 8.dp)
            ) {
                SearchBar(
                    modifier = Modifier.align(Alignment.TopCenter),
                    query = searchText,
                    onQueryChange = { searchText = it },
                    onSearch = {
                        // Can be used to trigger a search action, e.g., if you were calling an API
                        // For local filtering, the LaunchedEffect already handles it.
                        isActive = false // Close search bar after search action
                        Log.d("ExpensesScreen", "Search triggered for: $it")
                    },
                    active = isActive,
                    onActiveChange = { isActive = it },
                    placeholder = { Text("Search Expenses") }, // stringResource(R.string.search_expenses)
                    leadingIcon = {
                        if (isActive) {
                            IconButton(onClick = { isActive = false; searchText = "" }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back"
                                ) // stringResource(R.string.back)
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search Icon")
                        }
                    },
                    trailingIcon = {
                        if (isActive && searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear Search"
                                ) // stringResource(R.string.clear_search)
                            }
                        } else if (!isActive && searchText.isNotEmpty()) { // Show clear when not active but text is present
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search")
                            }
                        }
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    // This is the content shown when the search bar is active (expanded)
                    // We will show the filtered results here directly
                    if (filteredExpenses.isEmpty() && searchText.isNotEmpty()) {
                        Text(
                            text = "No matching expenses found.", // stringResource(R.string.no_matching_expenses)
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(all = 8.dp)
                        ) {
                            items(filteredExpenses) { expense ->
                                ExpenseItemRow(expense)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding) // This padding is from the Scaffold (includes topBar height)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // When SearchBar is not active, the main content area shows the list
            // The SearchBar itself is either a small pill or a full-width bar above.
            if (!isActive) { // Only show this list if search is not active
                if (allExpenseItems.isEmpty()) {
                    Text(
                        "No expense records yet.", // stringResource(R.string.no_expenses_yet)
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else if (searchText.isNotEmpty() && filteredExpenses.isEmpty()) {
                    Text(
                        "No matching expenses found.",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        )
                    ) {
                        items(if (searchText.isBlank()) allExpenseItems else filteredExpenses) { expense ->
                            ExpenseItemRow(expense)
                        }
                    }
                }
            }
        }
    }
}

// Example Composable for displaying a single expense item
@Composable
fun ExpenseItemRow(item: MoneyItem) {
    val color = when {
        item.phone == "POCHI" -> Color(0xFFF51F2E)
        item.phone == "TILL_NO" -> Color(0xFFAD1457)
        item.phone == "PAYBILL" -> Color(0xFFB74075)
        else -> Color(0xFF9E9D24)
    }
    Column(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp) // Added horizontal padding for items
            .fillMaxWidth()
    ) {
        Text("${item.person}", style = MaterialTheme.typography.titleMedium)
        Text(
            formatCurrency(item.amount.toLong()),
            color = color,
            style = MaterialTheme.typography.bodyLarge
        )
        Text("${item.phone}", color = color, style = MaterialTheme.typography.labelMedium)

        Text(
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.date)),
            style = MaterialTheme.typography.labelMedium
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}
