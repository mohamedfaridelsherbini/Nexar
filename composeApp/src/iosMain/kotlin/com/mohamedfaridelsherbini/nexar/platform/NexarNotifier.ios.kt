package com.mohamedfaridelsherbini.nexar.platform

/** iOS notifications are handled entirely by the native Swift `NexarNotificationService` actor. */
actual object NexarNotifier {
    actual fun postDuplicateAlert(docName: String) = Unit

    actual fun scheduleExportReminderWorker() = Unit
}
