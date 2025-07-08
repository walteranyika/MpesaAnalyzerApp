package com.walter.mbogo.workers //

import android.content.Context
import android.provider.Telephony
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.walter.mbogo.db.AppDatabase
import com.walter.mbogo.db.MoneyItem
import com.walter.mbogo.db.SimpleDataManager
import com.walter.mbogo.utility.ProcessedMessage
import com.walter.mbogo.utility.analyzePaybillMessages
import com.walter.mbogo.utility.analyzeReceivedMessages
import com.walter.mbogo.utility.analyzeSentMessages
import com.walter.mbogo.utility.analyzeTillMessages
import kotlinx.coroutines.flow.first

class MpesaSmsWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val moneyDao = AppDatabase.getDatabase(applicationContext).moneyDao()
        val dataManager = SimpleDataManager(applicationContext)
        try {
            Log.d("MpesaSmsWorker", "Starting MPESA SMS processing.")
            // 1. Get last processed timestamp (if implementing delta-updates)
            var lastTimestamp = dataManager.lastReadTimestampFlow.first()

            val contentResolver = applicationContext.contentResolver
            val projection = arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            )
            // Add selection and selectionArgs to filter messages
            // Be mindful of performance with very large inboxes.
            val selection = Telephony.Sms.ADDRESS + " = ? AND " + Telephony.Sms.DATE + " >= ?"
            val selectionArgs = arrayOf("MPESA", lastTimestamp.toString())//, lastTimestamp.toString()
            Log.d("MpesaSmsWorker", "Last Timestamp $lastTimestamp")

            val cursor = contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                selection, // Replace with your selection criteria
                selectionArgs, // Replace with your selection arguments
                "date DESC" // Or sort by date
            )

            cursor?.use {
                Log.d("MpesaSmsWorker", "Started processing")
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS) // If needed

                while (it.moveToNext()) {
                    val body = it.getString(bodyIndex)
                    val date = it.getLong(dateIndex)
                    val address = it.getString(addressIndex)
                    Log.d("MpesaSmsWorker", "Last Timestamp ${lastTimestamp} Against $date")

                    if (date > lastTimestamp) {
                        lastTimestamp = date
                    }

                    var result: ProcessedMessage? = null
                    result = if (body.contains("You have received")) {
                        analyzeReceivedMessages(body, date)
                    } else if (body.contains("sent to ") && body.contains("for account")) {
                        analyzePaybillMessages(body, date)
                    } else if (body.contains("sent to ")) {
                        analyzeSentMessages(body, date)
                    } else if (body.contains("paid to ")) {
                        analyzeTillMessages(body, date)
                    } else {
                        null
                    }
                    if (result != null) {
                        val moneyItem = MoneyItem(
                            amount = result.amount,
                            person = result.name,
                            type = result.type,
                            phone = result.phone,
                            date = result.date,
                            code = result.code
                        )
                        moneyDao.insertMoneyItem(moneyItem)
                        Log.d("MpesaSmsWorker", "Inserted: $moneyItem")
                    }
                }
                dataManager.storeLastRead(lastTimestamp)
            }
            Log.d("MpesaSmsWorker", "MPESA SMS processing finished. $lastTimestamp")
            return Result.success()
        } catch (e: Exception) {
            Log.e("MpesaSmsWorker", "Error processing MPESA SMS", e)
            return Result.failure()
        }
    }
}