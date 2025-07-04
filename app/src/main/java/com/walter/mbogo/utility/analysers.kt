package com.walter.mbogo.utility

import android.util.Log
import androidx.compose.ui.text.toUpperCase
import java.util.Locale

fun analyzeReceivedMessages(body: String, date: Long): ProcessedMessage {
    Log.d("PROCESSING_MESSAGE", "analyzeReceivedMessages: $body")

    val code = body.substring(0..10).trim()
    val nonAlpha = "[a-zA-Z]".toRegex()
    val amount =
        body.substring(body.indexOf("You have received"), body.indexOf("from ")).replace(nonAlpha, "")
            .replace(",", "").trim()
    val person =
        body.substring(body.indexOf("from "), body.indexOf(" on ")).replace("from", "")
            .replace("[0-9]".toRegex(), "").trim()

    var number = body.substring(body.indexOf(" from "), body.indexOf(" on "))
        .replace("[a-zA-Z]".toRegex(), "").trim()
    if (number.isEmpty()){
      number = person.replace(" ", "_").uppercase(Locale.getDefault())
    }
    return ProcessedMessage(code=code, phone=number, name=person, amount=amount.toDouble(), date=date, type = TransactionTypes.INCOME)
}

fun analyzeSentMessages(body: String, date: Long): ProcessedMessage {
    Log.d("PROCESSING_MESSAGE", "analyzeSentMessages: $body")
    val code = body.substring(0..10).trim()
    val nonAlpha = "[a-zA-Z]".toRegex()

    val amount =
        body.substring(body.indexOf("Confirmed"), body.indexOf("sent to ")).replace(nonAlpha, "")
            .replace(",", "").replace(". ", "").trim()
    val startIndex = body.indexOf("sent to ");
    val person =
        body.substring(body.indexOf("sent to "), body.indexOfAny(listOf("07", "01", " on "), startIndex+5)).replace("sent to ", "")
            .replace("[0-9]".toRegex(), "").trim()

    var number = body.substring(body.indexOf(" sent to "), body.indexOf(" on "))
        .replace("[a-zA-Z]".toRegex(), "").replace("'", "").trim()
    if (number.isEmpty()){
        number = person.replace(" ", "").uppercase(Locale.getDefault())
    }
    return ProcessedMessage(code=code, phone=number, name=person, amount=amount.toDouble(), date=date, type = TransactionTypes.EXPENSE)
}