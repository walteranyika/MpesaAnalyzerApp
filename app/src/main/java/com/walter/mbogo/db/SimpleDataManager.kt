package com.walter.mbogo.db

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.myDataStore by preferencesDataStore(name = "import_prefs")

class SimpleDataManager (
    private val context: Context
) {
    // Create keys to store and retrieve the data
    companion object {
        val LAST_READ_SMS_TIMESTAMP = longPreferencesKey("LAST_READ_SMS_TIMESTAMP")
    }

    // function to store user data
    suspend fun storeLastRead(age: Long) {
        context.myDataStore.edit {
            it[LAST_READ_SMS_TIMESTAMP] = age }
    }

    // Create an age flow to retrieve age from the preferences
    val lastReadTimestampFlow: Flow<Long> = context.myDataStore.data.map {
        it[LAST_READ_SMS_TIMESTAMP] ?: 0
    }

}