package com.walter.mbogo.workers //

import android.content.Context
import android.provider.Telephony
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.walter.mbogo.db.AppDatabase
import com.walter.mbogo.db.MoneyItem
import com.walter.mbogo.utility.TransactionTypes

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
            val cursor = contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                null, // Replace with your selection criteria
                null, // Replace with your selection arguments
                Telephony.Sms.DEFAULT_SORT_ORDER // Or sort by date
            )

            cursor?.use {
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS) // If needed

                while (it.moveToNext()) {
                    val body = it.getString(bodyIndex)
                    val date = it.getLong(dateIndex)
                    val address = it.getString(addressIndex)

                    // TODO: Your sophisticated MPESA message parsing logic here
                    // This is a placeholder for your actual parsing
                    if (body != null && body.contains("MPESA", ignoreCase = true) /* more specific checks */) {
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
                    }
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

    // Placeholder for your actual parsing logic
    private fun parseMpesaMessageToMoneyItem(body: String, date: Long): MoneyItem? {
        // Implement your full parsing logic here.
        // This is a very simplified example.
        var type = ""
        var amount = 0.0
        var person = "Unknown"
        var phone: String? = null

        if (body.contains("You have received")) {
            type = TransactionTypes.INCOME // Assuming you have this constant
            // Extract amount, person etc.
            // Example: "You have received Ksh100.00 from JOHN DOE 07XXYYYYYY..."
            // This requires robust regex or string manipulation
            try {
                val amountRegex = "Ksh([\\d,]+\\.\\d{2})".toRegex()
                amount = amountRegex.find(body)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

                val fromRegex = "from ([A-Z .]+) (\\d{10})".toRegex() // Simple example
                val matchResult = fromRegex.find(body)
                if (matchResult != null) {
                    person = matchResult.groupValues[1].trim()
                    phone = matchResult.groupValues[2]
                } else {
                    val fromAgentRegex = "from Agent no \\d+ - (.+)".toRegex()
                    val agentMatch = fromAgentRegex.find(body)
                    if(agentMatch != null) person = agentMatch.groupValues[1].trim()
                }

            } catch (e: Exception) { Log.e("Parser", "Error parsing income: $body", e); return null }

        } else if (body.contains("sent to")) {
            type = TransactionTypes.EXPENSE
            // Extract amount, person etc.
            // Example: "ABCDEF1234 Confirmed. Ksh200.00 sent to JANE DOE 07XXYYYYYY..."
            try {
                val amountRegex = "Ksh([\\d,]+\\.\\d{2})".toRegex()
                amount = amountRegex.find(body)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

                val toRegex = "sent to ([A-Z .]+) (\\d{10})".toRegex()
                val matchResult = toRegex.find(body)
                if (matchResult != null) {
                    person = matchResult.groupValues[1].trim()
                    phone = matchResult.groupValues[2]
                } else {
                    // Handle cases like "paid to XYZ LTD" or "Buy Goods"
                    val paidToRegex = "(?:paid to|Buy Goods and Services to) ([A-Z0-9 .\\-&_]+)".toRegex(RegexOption.IGNORE_CASE)
                    val paidToMatch = paidToRegex.find(body)
                    if(paidToMatch != null) person = paidToMatch.groupValues[1].trim()
                }


            } catch (e: Exception) { Log.e("Parser", "Error parsing expense: $body", e); return null }
        } else {
            return null // Not a recognized MPESA message format
        }

        if (type.isNotEmpty() && amount > 0) {
            return MoneyItem(person = person, type = type, phone = phone, date = date, amount = amount)
        }
        return null
    }
}