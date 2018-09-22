package com.example.smsreader.activity

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.media.RingtoneManager
import android.media.RingtoneManager.getDefaultUri
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.telephony.SmsMessage
import android.view.View
import com.example.smsreader.R
import com.example.smsreader.adapter.SMSAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : BaseActivity(), LoaderManager.LoaderCallbacks<Cursor>, SMSAdapter.OnItemClickListener {

    companion object {
        private const val TODAY = 0
        private const val YESTERDAY = 1
        private const val EARLIER = 2
        private const val NOTIFICATION_ID = 100
        private const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID"
        const val DAY_MILLISECONDS: Long = 24 * 60 * 60 * 1000
        const val SMS_BODY = "body"
        const val SMS_CREATOR = "creator"
        const val SMS_ADDRESS = "address"
        const val SMS_DATE = "date"
        const val SMS_ID = "_id"
    }

    private val permissionList = arrayOf(Manifest.permission.READ_SMS)
    private lateinit var smsReceiver: BroadcastReceiver

    private lateinit var todaySmsAdapter: SMSAdapter
    private lateinit var yestSmsAdapter: SMSAdapter
    private lateinit var earlierSmsAdapter: SMSAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()

        requestPermission(permissionList, object : PermissionCallback {
            override fun onPermissionGranted(grantedPermissions: Array<String>) {
                supportLoaderManager.initLoader(TODAY, null, this@MainActivity)
                supportLoaderManager.initLoader(YESTERDAY, null, this@MainActivity)
                supportLoaderManager.initLoader(EARLIER, null, this@MainActivity)
            }

            override fun onPermissionDenied(deniedPermissions: Array<String>) {
                showSnackMessage("SMS permission required")
            }

            override fun onPermissionBlocked(blockedPermissions: Array<String>) {
                showSnackMessage("SMS permission required")
            }

        })
    }

    override fun onStart() {
        super.onStart()

        val intentFilter = IntentFilter()
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val bundle = intent!!.extras
                if (bundle != null) {

                    val pdusArr = bundle.get("pdus") as Array<Any>

                    for (i in pdusArr.indices) {
                        val currentMessage = SmsMessage.createFromPdu(pdusArr[i] as ByteArray)
                        val senderNum = currentMessage.displayOriginatingAddress
                        val message = currentMessage.displayMessageBody

                        setNotification(senderNum, message, currentMessage.timestampMillis)
                    }
                }
            }

        }

        registerReceiver(smsReceiver, intentFilter)
    }

    private fun initRecyclerView() {
        // Setup Today SMS RecyclerView
        val todayRecyclerView = findViewById<RecyclerView>(R.id.today_recycler_view)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        todayRecyclerView.layoutManager = linearLayoutManager
        val dividerItemDecoration = DividerItemDecoration(this, linearLayoutManager.getOrientation())
        todayRecyclerView.addItemDecoration(dividerItemDecoration)
        todaySmsAdapter = SMSAdapter(this, this)
        todayRecyclerView.adapter = todaySmsAdapter

        // Setup Yesterday SMS RecyclerView
        val yestRecyclerView = findViewById<RecyclerView>(R.id.yesterday_recycler_view)
        val linearLayoutManager1 = LinearLayoutManager(this)
        linearLayoutManager1.orientation = LinearLayoutManager.VERTICAL
        yestRecyclerView.layoutManager = linearLayoutManager1
        val dividerItemDecoration1 = DividerItemDecoration(this, linearLayoutManager1.getOrientation())
        yestRecyclerView.addItemDecoration(dividerItemDecoration1)

        yestSmsAdapter = SMSAdapter(this, this)
        yestRecyclerView.adapter = yestSmsAdapter

        // Setup Today SMS RecyclerView
        val earlierRecyclerView = findViewById<RecyclerView>(R.id.earlier_recycler_view)
        val linearLayoutManager2 = LinearLayoutManager(this)
        linearLayoutManager2.orientation = LinearLayoutManager.VERTICAL
        earlierRecyclerView.layoutManager = linearLayoutManager2
        val dividerItemDecoration2 = DividerItemDecoration(this, linearLayoutManager2.getOrientation())
        earlierRecyclerView.addItemDecoration(dividerItemDecoration2)

        earlierSmsAdapter = SMSAdapter(this, this)
        earlierRecyclerView.adapter = earlierSmsAdapter
    }

    override fun onItemClicked(senderNum: String, message: String, smsDate: Long) {

        val intent = Intent(this, SMSDetailActivity::class.java)

        val bundle = Bundle()
        bundle.putString(SMS_ADDRESS, senderNum)
        bundle.putString(SMS_BODY, message)
        bundle.putLong(SMS_DATE, smsDate)

        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        // Create Inbox box URI
        val inboxURI = Uri.parse("content://sms/inbox")

        // List required columns
        val reqCols = arrayOf(SMS_ID, SMS_CREATOR, SMS_ADDRESS, SMS_BODY, SMS_DATE)

        val calendar = Calendar.getInstance()
        calendar.time = Date()

        var where: String? = null
        var sorting = "date desc"
        val oneDay: Long = calendar.timeInMillis - DAY_MILLISECONDS
        val secondDay: Long = calendar.timeInMillis - (2 * DAY_MILLISECONDS)
        when (id) {
            TODAY -> where = "date > " + calendar.timeInMillis + " AND date < " + oneDay
            YESTERDAY -> where = "date > $oneDay AND date >= $secondDay"
            EARLIER -> {
                where = "date < $secondDay"
                sorting = "date desc limit 100"
            }
        }

        // Fetch Inbox SMS Message from Built-in Content Provider
        return CursorLoader(this, inboxURI, reqCols, where, null, sorting)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        when (loader.id) {
            TODAY -> {
                if (data!!.count == 0) {
                    today.visibility = View.GONE
                } else {
                    today.visibility = View.VISIBLE
                }
                todaySmsAdapter.swapCursor(data)
            }
            YESTERDAY -> {
                if (data!!.count == 0) {
                    yesterday.visibility = View.GONE
                } else {
                    yesterday.visibility = View.VISIBLE
                }
                yestSmsAdapter.swapCursor(data)
            }
            EARLIER -> {
                if (data!!.count == 0) {
                    earlier.visibility = View.GONE
                } else {
                    earlier.visibility = View.VISIBLE
                }
                earlierSmsAdapter.swapCursor(data)
            }
        }

    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Do nothing for now
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsReceiver)
    }

    private fun setNotification(title: String, notificationMessage: String, smsTime: Long) {
        val requestID = System.currentTimeMillis().toInt()

        val alarmSound = getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager = NotificationManagerCompat.from(this)

        val notificationIntent = Intent(applicationContext, SMSDetailActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val bundle = Bundle()
        bundle.putString(SMS_ADDRESS, title)
        bundle.putString(SMS_BODY, notificationMessage)
        bundle.putLong(SMS_DATE, smsTime)

        notificationIntent.putExtras(bundle)

        val contentIntent = PendingIntent.getActivity(this, requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.navigation_empty_icon)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(notificationMessage))
                .setContentText(notificationMessage).setAutoCancel(true)
        mBuilder.setSound(alarmSound)
        mBuilder.setContentIntent(contentIntent)

        notificationManager.notify(NOTIFICATION_ID, mBuilder.build())
    }

}
