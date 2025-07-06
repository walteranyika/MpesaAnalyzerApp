package com.walter.mbogo.workers //

import android.content.Context
import android.provider.Telephony
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.walter.mbogo.db.AppDatabase
import com.walter.mbogo.db.MoneyItem
import com.walter.mbogo.utility.ProcessedMessage
import com.walter.mbogo.utility.analyzeReceivedMessages
import com.walter.mbogo.utility.analyzeSentMessages

class MpesaSmsWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val moneyDao = AppDatabase.getDatabase(applicationContext).moneyDao()

        try {
            Log.d("MpesaSmsWorker", "Starting MPESA SMS processing.")
            // --- Your SMS Reading and Parsing Logic ---
            // 1. Get last processed timestamp (if implementing delta-updates)
            //    val lastTimestamp = inputData.getLong("LAST_PROCESSED_TIMESTAMP", 0L)
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
            val selection = Telephony.Sms.ADDRESS + " = ?"
            val selectionArgs = arrayOf("MPESA")
            val cursor = contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                selection, // Replace with your selection criteria
                selectionArgs, // Replace with your selection arguments
                "date DESC" // Or sort by date
            )

            cursor?.use {
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS) // If needed

                while (it.moveToNext()) {
                    val body = it.getString(bodyIndex)
                    val date = it.getLong(dateIndex)
                    val address = it.getString(addressIndex)

                    var result : ProcessedMessage? = null
                    if (body.contains("You have received")) {
                         result = analyzeReceivedMessages(body, date)
                    }else if(body.contains("sent to ")){
                          result = analyzeSentMessages(body, date)
                    }else{
                        result = null
                    }

                    Log.d("PROCESSED", "doWork: $result")
                    if (result != null){
                        val moneyItem = MoneyItem(amount = result.amount, person = result.name, type = result.type, phone = result.phone, date = result.date, code = result.code)
                        moneyDao.insertMoneyItem(moneyItem)
                        Log.d("MpesaSmsWorker", "Inserted: $moneyItem")
                    }
                    // TODO: Your sophisticated MPESA message parsing logic here
                    // This is a placeholder for your actual parsing
                   /* if (body != null && body.contains("MPESA", ignoreCase = true) *//* more specific checks *//*) {
                        // --- Extracted from your logic ---
                        // val person: String = extractPerson(body)
                        // val type: String = extractType(body) // "INCOME" or "EXPENSE"
                        // val phone: String? = extractPhone(body)
                        // val amount: Double = extractAmount(body)
                        // val transactionDate: Long = date // from SMS

                        // For example:
                        val moneyItem = parseMpesaMessageToMoneyItem(body, date)

                        if (moneyItem != null) {
                            moneyDao.insertMoneyItem(moneyItem)
                            Log.d("MpesaSmsWorker", "Inserted: $moneyItem")
                        }
                    }*/
                }
            }
            Log.d("MpesaSmsWorker", "MPESA SMS processing finished.")
            // TODO: Store the timestamp of the latest processed message for delta updates
            return Result.success()
        } catch (e: Exception) {
            Log.e("MpesaSmsWorker", "Error processing MPESA SMS", e)
            return Result.failure()
        }
    }
}