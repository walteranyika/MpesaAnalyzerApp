package com.walter.mbogo.utility

import android.util.Log
import com.walter.mbogo.viewmodels.TransactionTypes
import java.util.Arrays
import java.util.Locale

fun analyzeReceivedMessages(body: String, date: Long): ProcessedMessage {
    Log.d("PROCESSING_MESSAGE", "analyzeReceivedMessages: $body")

    val code = body.substring(0..10).trim()
    val nonAlpha = "[a-zA-Z]".toRegex()
    val amount =
        body.substring(body.indexOf("You have received"), body.indexOf("from ")).replace(nonAlpha, "")
            .replace(",", "").trim()
    var person =
        body.substring(body.indexOf("from "), body.indexOf(" on ")).replace("from", "")
            .replace("[0-9]".toRegex(), "").trim()

    var number = body.substring(body.indexOf(" from "), body.indexOf(" on "))
        .replace("[a-zA-Z]".toRegex(), "").trim().replace(". ", "").replace("-", "").trim()
    val matches = Arrays.stream(arrayOf("BULK PAYMENT", "Cadana Inc", "CADANA TECHNOLOGIES","CHOICE MICROFINANCE","DUKAPAY")).anyMatch(person::contains)
    if (matches){
        person = "Cadana"
    }
    if (number.isEmpty() || person.equals("Cadana", ignoreCase = true)){
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
    var person =
        body.substring(body.indexOf("sent to "), body.indexOfAny(listOf("07", "01", " on "), startIndex+5)).replace("sent to ", "")
            .replace("[0-9]".toRegex(), "").replace("+", "").replace(".", "").replace("for account deposit", "").trim()

    var number = body.substring(body.indexOf(" sent to "), body.indexOf(" on "))
        .replace("[a-zA-Z]".toRegex(), "").replace("'", "").replace(".", "").replace("*", "").replace("-", "").trim()
    val matches = Arrays.stream(arrayOf("POSTPAID BUNDLES", "Safaricom Offers")).anyMatch(person::contains)
    if (matches){
        person = "Safaricom"
    }
    if (number.isEmpty()){
        number = person.replace(" ", "_").uppercase(Locale.getDefault())
    }

    return ProcessedMessage(code=code, phone=number, name=person, amount=amount.toDouble(), date=date, type = TransactionTypes.EXPENSE)
}