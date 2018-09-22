package com.example.smsreader.adapter

import android.content.Context
import android.database.Cursor
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.smsreader.R
import com.example.smsreader.Util
import com.example.smsreader.activity.MainActivity

import com.example.smsreader.custom.RecyclerViewCursorAdapter
import com.example.smsreader.custom.RecyclerViewCursorViewHolder

class SMSAdapter
/**
 * Constructor.
 * @param context The Context the Adapter is displayed in.
 * @param listener: OnItemClickListener
 */
(private val context: Context, private val listener: OnItemClickListener) : RecyclerViewCursorAdapter<SMSAdapter.MovieViewHolder>(context) {

    init {
        setupCursorAdapter(null, 0, R.layout.list_item_sms, false)
    }

    /**
     * Returns the ViewHolder to use for this adapter.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder(mCursorAdapter.newView(context, mCursorAdapter.cursor, parent))
    }

    /**
     * Moves the Cursor of the CursorAdapter to the appropriate position and binds the view for
     * that item.
     */
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        // Move cursor to this position
        val cursor = mCursorAdapter.cursor
        cursor.moveToPosition(position)

        // Set the ViewHolder
        setViewHolder(holder)

        // Bind this view
        mCursorAdapter.bindView(holder.itemView, context, cursor)

        holder.itemView.setOnClickListener {
            val senderNum = cursor.getString(cursor.getColumnIndex(MainActivity.SMS_ADDRESS))
            val message = cursor.getString(cursor.getColumnIndex(MainActivity.SMS_BODY))
            val smsDate = cursor.getLong(cursor.getColumnIndex(MainActivity.SMS_DATE))
            listener.onItemClicked(senderNum, message, smsDate)
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(senderNum: String, message: String, smsDate: Long)
    }

    /**
     * ViewHolder used to display a movie name.
     */
    inner class MovieViewHolder(view: View) : RecyclerViewCursorViewHolder(view) {
        private var senderName: TextView = view.findViewById(R.id.sms_sender_name) as TextView
        private var message: TextView = view.findViewById(R.id.message) as TextView
        private var smsDate: TextView = view.findViewById(R.id.sms_date) as TextView

        override fun bindCursor(cursor: Cursor) {
            senderName.text = cursor.getString(cursor.getColumnIndex(MainActivity.SMS_ADDRESS))
            message.text = cursor.getString(cursor.getColumnIndex(MainActivity.SMS_BODY))
            smsDate.text = Util.getDatefromTimeStamp(cursor.getLong(cursor.getColumnIndex(MainActivity.SMS_DATE)))
        }
    }
}