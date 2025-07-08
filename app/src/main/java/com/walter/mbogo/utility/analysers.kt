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
        body.substring(body.indexOf("You have received"), body.indexOf("from "))
            .replace(nonAlpha, "")
            .replace(",", "").trim()
    var person =
        body.substring(body.indexOf("from "), body.indexOf(" on ")).replace("from", "")
            .replace("[0-9]".toRegex(), "").trim()

    var number = body.substring(body.indexOf(" from "), body.indexOf(" on "))
        .replace("[a-zA-Z]".toRegex(), "").trim().replace(". ", "").replace("-", "").trim()
    val matches = Arrays.stream(
        arrayOf(
            "BULK PAYMENT",
            "Cadana Inc",
            "CADANA TECHNOLOGIES",
            "CHOICE MICROFINANCE BANK",
            "DUKAPAY"
        )
    ).anyMatch(person::contains)
    if (matches) {
        person = "Cadana"
        number = "Cadana"
    }
    if (number.isEmpty() || person.equals("Cadana", ignoreCase = true)) {
        number = person.replace(" ", "_").uppercase(Locale.getDefault())
    }

    return ProcessedMessage(
        code = code,
        phone = number,
        name = person,
        amount = amount.toDouble(),
        date = date,
        type = TransactionTypes.INCOME
    )
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
        body.substring(
            body.indexOf("sent to "),
            body.indexOfAny(listOf("07", "01", " on "), startIndex + 5)
        ).replace("sent to ", "")
            .replace("[0-9]".toRegex(), "").replace("+", "").replace(".", "")
            .replace("for account deposit", "").replace("M-PESA CARD for account","").replace("for account","").trim()

    var number = body.substring(body.indexOf(" sent to "), body.indexOf(" on "))
        .replace("[a-zA-Z]".toRegex(), "").replace("'", "").replace(".", "").replace("*", "")
        .replace("-", "").trim()
    val matches =
        Arrays.stream(arrayOf("POSTPAID BUNDLES", "Safaricom Offers")).anyMatch(person::contains)
    if (matches) {
        person = "Safaricom"
    }
    if (number.isEmpty()) {
        number = "POCHI"
    }

    return ProcessedMessage(
        code = code,
        phone = number,
        name = person,
        amount = amount.toDouble(),
        date = date,
        type = TransactionTypes.EXPENSE
    )
}


fun analyzeTillMessages(body: String, date: Long): ProcessedMessage {
    Log.d("PROCESSING_MESSAGE", "analyzeTillMessages: $body")
    val code = body.substring(0..10).trim()
    val nonAlpha = "[a-zA-Z]".toRegex()
    val amount =
        body.substring(body.indexOf("Ksh"), body.indexOf("paid to ")).replace(nonAlpha, "")
            .replace(",", "").trim()
    val person =
        body.substring(body.indexOf("paid to"), body.indexOf(" on ")).replace("paid to ", "")
            .replace("[0-9]".toRegex(), "").replace("+", "").replace(".", "").replace(".", "")
            .trim()
    val number = "TILL"
    return ProcessedMessage(
        code = code,
        phone = number,
        name = person,
        amount = amount.toDouble(),
        date = date,
        type = TransactionTypes.EXPENSE
    )
}


fun analyzePaybillMessages(body: String, date: Long): ProcessedMessage {
    Log.d("PROCESSING_MESSAGE", "analyzePaybillMessages: $body")
    val code = body.substring(0..10).trim()
    val nonAlpha = "[a-zA-Z]".toRegex()
    val amount =
        body.substring(body.indexOf("Ksh"), body.indexOf("sent to ")).replace(nonAlpha, "")
            .replace(",", "").trim()
    val person =
        body.substring(body.indexOf("sent to"), body.indexOf(" for account ")).replace("sent to ", "")
            .replace("[0-9]".toRegex(), "").replace("+", "").replace(".", "").replace(".", "")
            .trim()
    val number = "PAYBILL"
    return ProcessedMessage(
        code = code,
        phone = number,
        name = person,
        amount = amount.toDouble(),
        date = date,
        type = TransactionTypes.EXPENSE
    )
}