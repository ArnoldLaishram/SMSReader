package com.example.smsreader.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateFormat
import com.example.smsreader.R
import com.example.smsreader.Util
import kotlinx.android.synthetic.main.activity_smsdetail.*
import java.util.*

class SMSDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smsdetail)

        val title = intent.extras.getString(MainActivity.SMS_ADDRESS)
        val sms = intent.extras.getString(MainActivity.SMS_BODY)
        val smsTime = intent.extras.getLong(MainActivity.SMS_DATE)

        val date = Util.getDatefromTimeStamp(smsTime)

        sms_sender_name.text = title
        message.text = sms
        sms_date.text = date
    }
}
