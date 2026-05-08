package com.mohamedfaridelsherbini.nexar.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mohamedfaridelsherbini.nexar.MainActivity
import com.mohamedfaridelsherbini.nexar.notifications.ExportReminderWorker
import java.util.concurrent.TimeUnit

private const val CH_DUPLICATE = "nexar_duplicate_alert"
private const val CH_EXPORT = "nexar_export_reminder"
private const val WORK_TAG = "nexar_export_reminder"
private var notifId = 1000

private lateinit var appCtx: Context

/** Called once in `NexarApplication.onCreate()` — must run before any notification is posted. */
fun initNexarNotifier(context: Context) {
    appCtx = context.applicationContext
    NexarNotifier.createChannels(appCtx)
}

actual object NexarNotifier {

    actual fun postDuplicateAlert(docName: String) {
        if (!hasPermission()) return
        val nm = appCtx.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(appCtx, CH_DUPLICATE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Possible duplicate detected")
            .setContentText("'$docName' looks similar to an existing document")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(launchAppIntent())
            .setAutoCancel(true)
            .build()
        nm.notify(notifId++, notification)
    }

    actual fun scheduleExportReminderWorker() {
        val request = PeriodicWorkRequestBuilder<ExportReminderWorker>(24L, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        WorkManager.getInstance(appCtx).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Called from [ExportReminderWorker] when pending documents exist. */
    fun postExportReminderNotification(count: Int) {
        if (!hasPermission()) return
        val nm = appCtx.getSystemService(NotificationManager::class.java)
        val body = if (count == 1) "1 document is ready to export"
                   else "$count documents are ready to export"
        val notification = NotificationCompat.Builder(appCtx, CH_EXPORT)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setContentTitle("Documents waiting in Nexar")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(launchAppIntent())
            .setAutoCancel(true)
            .build()
        nm.notify(notifId++, notification)
    }

    internal fun createChannels(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CH_DUPLICATE, "Duplicate Alerts", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Alerts when a possible duplicate document is detected" }
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_EXPORT, "Export Reminders", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Daily reminders to export pending documents" }
        )
    }

    private fun hasPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                appCtx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    /** Returns a [PendingIntent] that brings [MainActivity] to the foreground when tapped. */
    private fun launchAppIntent(): PendingIntent {
        val intent = Intent(appCtx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            appCtx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
