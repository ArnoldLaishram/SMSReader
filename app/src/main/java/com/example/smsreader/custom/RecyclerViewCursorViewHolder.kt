package com.example.smsreader.custom

import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * ViewHolder to be used with a RecyclerViewCursorAdapter.
 */
abstract class RecyclerViewCursorViewHolder
/**
 * Constructor.
 * @param view The root view of the ViewHolder.
 */
(view: View) : RecyclerView.ViewHolder(view) {

    /**
     * Binds the information from a Cursor to the various UI elements of the ViewHolder.
     * @param cursor A Cursor representation of the data to be displayed.
     */
    abstract fun bindCursor(cursor: Cursor)
}