package com.mohamedfaridelsherbini.nexar.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class NexarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NexarWidget()

    companion object {
        fun notifyUpdate(context: Context) {
            MainScope().launch {
                val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(NexarWidget::class.java)
                glanceIds.forEach { glanceId ->
                    NexarWidget().update(context, glanceId)
                }
            }
        }
    }
}
