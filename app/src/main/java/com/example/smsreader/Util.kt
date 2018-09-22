package com.example.smsreader

import android.text.format.DateFormat
import java.util.*

object Util {

    fun getDatefromTimeStamp(smsTime: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = smsTime

        return DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString()
    }
}