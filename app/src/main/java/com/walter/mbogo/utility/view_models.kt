package com.walter.mbogo.utility

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.walter.mbogo.db.AppDatabase
import com.walter.mbogo.db.MoneyDao
import com.walter.mbogo.db.MoneyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val moneyDao: MoneyDao
    val allIncomes: LiveData<List<MoneyItem>>
    val allExpenses: LiveData<List<MoneyItem>>

    init {
        moneyDao = AppDatabase.getDatabase(application).moneyDao()
        // Use your defined constants if available, otherwise use string literals directly
        allIncomes = moneyDao.getItemsByType(TransactionTypes.INCOME /* or "INCOME" */).asLiveData()
        allExpenses = moneyDao.getItemsByType(TransactionTypes.EXPENSE /* or "EXPENSE" */).asLiveData()
    }

    /**
     * Inserts a new MoneyItem into the database.
     * This operation is performed on a background thread.
     */
    fun insert(moneyItem: MoneyItem) = viewModelScope.launch(Dispatchers.IO) {
        moneyDao.insertMoneyItem(moneyItem)
    }

    /**
     * Deletes a MoneyItem from the database.
     * This operation is performed on a background thread.
     */
    fun delete(moneyItem: MoneyItem) = viewModelScope.launch(Dispatchers.IO) {
        moneyDao.deleteMoneyItem(moneyItem)
    }

    // You can add other methods here to interact with the DAO, for example:
    // fun getItemsByPhone(phoneNumber: String): LiveData<List<MoneyItem>> {
    //     return moneyDao.getItemsByPhone(phoneNumber).asLiveData()
    // }

    // fun getItemsByName(name: String): LiveData<List<MoneyItem>> {
    //     return moneyDao.getItemsByName(name).asLiveData()
    // }

    // fun getTotalAmountGroupedByPhone(): LiveData<List<PhoneTotal>> {
    //    return moneyDao.getTotalAmountGroupedByPhone().asLiveData()
    // }
}


object TransactionTypes {
    const val INCOME = "INCOME"
    const val EXPENSE = "EXPENSE"
}