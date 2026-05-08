package com.mohamedfaridelsherbini.nexar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mohamedfaridelsherbini.nexar.MainActivity
import com.mohamedfaridelsherbini.nexar.R

class NexarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        private const val PREFS_NAME = "nexar_widget_prefs"
        private const val KEY_PENDING_COUNT = "pending_count"
        private const val KEY_LAST_SCAN = "last_scan_name"

        fun saveData(context: Context, pendingCount: Int, lastScanName: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
                putInt(KEY_PENDING_COUNT, pendingCount)
                putString(KEY_LAST_SCAN, lastScanName)
                apply()
            }
            notifyUpdate(context)
        }

        fun notifyUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, NexarWidgetProvider::class.java)
            )
            for (id in ids) updateWidget(context, manager, id)
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val pendingCount = prefs.getInt(KEY_PENDING_COUNT, 0)
            val lastScan = prefs.getString(KEY_LAST_SCAN, "No scans yet") ?: "No scans yet"

            val views = RemoteViews(context.packageName, R.layout.widget_nexar).apply {
                setTextViewText(R.id.widget_pending_count,
                    "$pendingCount pending export${if (pendingCount != 1) "s" else ""}"
                )
                setTextViewText(R.id.widget_last_scan, lastScan)

                val launchIntent = Intent(context, MainActivity::class.java)
                val pendingLaunch = PendingIntent.getActivity(
                    context, 0, launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_scan_btn, pendingLaunch)
                setOnClickPendingIntent(R.id.widget_icon, pendingLaunch)
            }

            manager.updateAppWidget(widgetId, views)
        }
    }
}
