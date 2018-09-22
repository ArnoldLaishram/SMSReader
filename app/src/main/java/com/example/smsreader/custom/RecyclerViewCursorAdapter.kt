package com.example.smsreader.custom

import android.content.Context
import android.database.Cursor
import android.support.v4.widget.CursorAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Adapter class that uses a CursorAdapter to display data in a RecyclerView.
 *
 */
abstract class RecyclerViewCursorAdapter<T : RecyclerViewCursorViewHolder>
/**
 * Constructor.
 * @param context The Context the Adapter is displayed in.
 */
protected constructor(
        /**
         * The Context of the adapter.
         */
        protected val mContext: Context) : RecyclerView.Adapter<T>() {

    /**
     * The CursorAdapter to display data with.
     */
    protected lateinit var mCursorAdapter: CursorAdapter

    /**
     * ViewHolder object to bind Cursor data to. A class level variable is created to pass the
     * ViewHolder between RecyclerView.Adapter.bindView() and CursorAdapter.bindView()
     */
    private var mViewHolder: T? = null

    /**
     * Default implementation of the CursorAdapter for the scenario when only one view type is used.
     * @param cursor The Cursor from which to get the data.
     * @param flags Flags used to determine the behavior of the adapter.
     * @param resource Resource ID for an XML layout to be inflated.
     * @param attachToRoot Whether the inflated layout should be attached to the root view.
     */
    protected fun setupCursorAdapter(cursor: Cursor?, flags: Int, resource: Int, attachToRoot: Boolean) {
        this.mCursorAdapter = object : CursorAdapter(mContext, cursor, flags) {
            override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
                return LayoutInflater.from(context).inflate(resource, parent, attachToRoot)
            }

            override fun bindView(view: View, context: Context, cursor: Cursor) {
                // Bind cursor to our ViewHolder
                mViewHolder!!.bindCursor(cursor)
            }
        }
    }

    /**
     * Swap the Cursor of the CursorAdapter and notify the RecyclerView.Adapter that data has
     * changed.
     * @param cursor The new Cursor representation of the data to be displayed.
     */
    fun swapCursor(cursor: Cursor) {
        this.mCursorAdapter.swapCursor(cursor)
        notifyDataSetChanged()
    }

    /**
     * The number of elements in the adapter is the number of elements in the CursorAdapter.
     */
    override fun getItemCount(): Int {
        return mCursorAdapter.count
    }

    /**
     * Sets the ViewHolder object.
     * @param viewHolder The ViewHolder we will be binding data to.
     */
    protected fun setViewHolder(viewHolder: T) {
        this.mViewHolder = viewHolder
    }
}