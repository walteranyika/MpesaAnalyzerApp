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
import androidx.compose.runtime.invalidateGroupsWithKey
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.walter.mbogo.db.PhoneTotal
import com.walter.mbogo.utility.formatCurrency
import com.walter.mbogo.viewmodels.TransactionTypes
import com.walter.mbogo.viewmodels.TransactionViewModel

@Composable
fun ChartsScreen(transactionViewModel: TransactionViewModel = viewModel()) {
    val allAnalysis by transactionViewModel.allAnalysis.observeAsState(initial = emptyList())

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (allAnalysis.isEmpty()) {
                Text("No records loaded yet.")
            } else {
                LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
                    items(allAnalysis) { data ->
                        // Replace this with your actual Composable to display an expense item
                        AnalysisItemRow(data)
                    }
                }
            }
        }
    }
}


// Example Composable for displaying a single expense item
@Composable
fun AnalysisItemRow(item: PhoneTotal) {
    val color = when (item.type) {
        "EXPENSE" -> Color(0xFFF51F2E)
        else ->  Color(0xFF28812D)
    }
    Column(modifier = Modifier
        .padding(vertical = 4.dp)
        .fillMaxWidth()) {
        Text("${item.person}")
        Text("${item.phone}")
        Text("${formatCurrency(item.totalAmount.toLong())}", color = color)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}