package com.mohamedfaridelsherbini.nexar.platform

/**
 * Platform notification bridge.
 *
 * Android: posts local notifications via NotificationCompat and schedules a periodic
 *          WorkManager job that checks for pending exports once a day.
 * iOS:     no-op — the Swift `NexarNotificationService` actor handles all iOS notifications.
 */
expect object NexarNotifier {
    /** Fires an immediate "possible duplicate" local notification. */
    fun postDuplicateAlert(docName: String)

    /** Enqueues the daily export-reminder worker (no-op if already queued). */
    fun scheduleExportReminderWorker()
}
