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
import com.walter.mbogo.utility.analyzeReceivedMessages
import com.walter.mbogo.utility.analyzeSentMessages
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last

class MpesaSmsWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val moneyDao = AppDatabase.getDatabase(applicationContext).moneyDao()
        val dataManager = SimpleDataManager(applicationContext)


        try {
            Log.d("MpesaSmsWorker", "Starting MPESA SMS processing.")
            // --- Your SMS Reading and Parsing Logic ---
            // 1. Get last processed timestamp (if implementing delta-updates)
            var lastTimestamp = dataManager.lastReadTimestampFlow.first()
            //    Or, for initial import, you might not need this, or query all.

            val contentResolver = applicationContext.contentResolver
            val projection = arrayOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            )
            // Add selection and selectionArgs to filter messages
            // e.g., Telephony.Sms.ADDRESS + " = ?", arrayOf("MPESA_SENDER_ID")
            // e.g., Telephony.Sms.DATE + " > ?", arrayOf(lastTimestamp.toString())
            // For initial scan, you might read a large batch or all relevant ones.
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
                    if (body.contains("You have received")) {
                        result = analyzeReceivedMessages(body, date)
                    } else if (body.contains("sent to ")) {
                        result = analyzeSentMessages(body, date)
                    } else {
                        result = null
                    }

                    Log.d("PROCESSED", "doWork: $result")
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