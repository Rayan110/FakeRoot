package com.fakeroot.manager.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fakeroot.manager.R
import com.fakeroot.manager.SettingsActivity
import com.fakeroot.manager.starter.Starter

/**
 * Adapter for the home screen RecyclerView
 */
class HomeAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<HomeItem>()
    private var onActionListener: OnActionListener? = null

    interface OnActionListener {
        fun onServiceStarted()
        fun onServiceStopped()
        fun onRefreshRequested()
    }

    fun setOnActionListener(listener: OnActionListener) {
        this.onActionListener = listener
    }

    companion object {
        private const val TYPE_STATUS = 0
        private const val TYPE_ACTION = 1
    }

    fun setItems(newItems: List<HomeItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeItem.Status -> TYPE_STATUS
            is HomeItem.Action -> TYPE_ACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_STATUS -> StatusViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_status, parent, false)
            )
            TYPE_ACTION -> ActionViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_action, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeItem.Status -> (holder as StatusViewHolder).bind(item)
            is HomeItem.Action -> (holder as ActionViewHolder).bind(item, context, onActionListener)
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * ViewHolder for status items
     */
    class StatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView = view.findViewById(R.id.title)
        private val summaryView: TextView = view.findViewById(R.id.summary)

        fun bind(item: HomeItem.Status) {
            titleView.text = itemView.context.getString(item.titleRes)
            summaryView.text = itemView.context.getString(item.summaryRes)
            titleView.setTextColor(ContextCompat.getColor(itemView.context, item.statusColor))
        }
    }

    /**
     * ViewHolder for action items
     */
    class ActionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView = view.findViewById(R.id.title)
        private val summaryView: TextView = view.findViewById(R.id.summary)

        fun bind(item: HomeItem.Action, context: Context, listener: OnActionListener?) {
            titleView.text = context.getString(item.titleRes)
            summaryView.text = context.getString(item.summaryRes)

            itemView.setOnClickListener {
                when (item.action) {
                    HomeItem.ActionType.START_SERVICE -> {
                        startService(context, listener)
                    }
                    HomeItem.ActionType.STOP_SERVICE -> {
                        stopService(context, listener)
                    }
                    HomeItem.ActionType.MANAGE_APPS -> {
                        // Open manage apps
                        Toast.makeText(context, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
                    }
                    HomeItem.ActionType.TERMINAL -> {
                        // Open terminal
                        Toast.makeText(context, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
                    }
                    HomeItem.ActionType.SETTINGS -> {
                        // Open settings
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            }
        }

        private fun startService(context: Context, listener: OnActionListener?) {
            if (!Starter.isIMQSNativeAvailable()) {
                Toast.makeText(context, R.string.imqs_not_available, Toast.LENGTH_LONG).show()
                return
            }

            Toast.makeText(context, R.string.starting_service, Toast.LENGTH_SHORT).show()

            Thread {
                try {
                    val result = Starter.startViaIMQSNative(context)
                    if (result is Starter.StarterResult.Success) {
                        // Wait a bit for service to start
                        Thread.sleep(1000)
                        listener?.onServiceStarted()
                    } else {
                        listener?.onRefreshRequested()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    listener?.onRefreshRequested()
                }
            }.start()
        }

        private fun stopService(context: Context, listener: OnActionListener?) {
            Toast.makeText(context, R.string.stopping_service, Toast.LENGTH_SHORT).show()

            Thread {
                try {
                    val result = Starter.stopService()
                    if (result is Starter.StarterResult.Success) {
                        Thread.sleep(500)
                        listener?.onServiceStopped()
                    } else {
                        listener?.onRefreshRequested()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    listener?.onRefreshRequested()
                }
            }.start()
        }
    }
}
