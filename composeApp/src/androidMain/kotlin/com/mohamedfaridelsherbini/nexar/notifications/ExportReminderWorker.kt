package com.mohamedfaridelsherbini.nexar.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import com.mohamedfaridelsherbini.nexar.platform.NexarNotifier
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Runs once every 24 hours.  Counts documents that have not yet been exported
 * and posts an export-reminder notification when the count is > 0.
 *
 * Scheduled by [NexarNotifier.scheduleExportReminderWorker] on first app launch.
 * Uses Koin component injection to obtain the [DocumentRepository].
 */
class ExportReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val repository: DocumentRepository by inject()

    override suspend fun doWork(): Result {
        val pendingCount = repository.observeDocuments().first()
            .count { !it.isExportedToStorage }
        if (pendingCount > 0) {
            NexarNotifier.postExportReminderNotification(pendingCount)
        }
        return Result.success()
    }
}
