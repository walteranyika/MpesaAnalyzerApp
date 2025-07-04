package com.walter.mbogo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.walter.mbogo.db.MoneyItem
import com.walter.mbogo.utility.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun IncomeScreen(transactionViewModel: TransactionViewModel = viewModel()) {
    val incomeItems by transactionViewModel.allIncomes.observeAsState(initial = emptyList())
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top, // Usually lists start from the top
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (incomeItems.isEmpty()) {
                Text("No income records yet.")
            } else {
                LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
                    items(incomeItems) { income ->
                        IncomeItemRow(income)
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeItemRow(item: MoneyItem) {
    Column(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Text("${item.person}")
        Text("Ksh ${item.amount}")
        Text(
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(java.util.Date(item.date))
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}