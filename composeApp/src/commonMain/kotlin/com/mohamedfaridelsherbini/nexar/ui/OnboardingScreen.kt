package com.mohamedfaridelsherbini.nexar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val actionLabel: String
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Outlined.Stars,
        title = "Welcome to Nexar",
        body = "Your intelligent document scanner that automatically reads, names, and organizes every document you capture.",
        actionLabel = "Get started"
    ),
    OnboardingPage(
        icon = Icons.Outlined.PhotoCamera,
        title = "Scan & Read",
        body = "Point your camera at any receipt, invoice, contract, ID, or medical record. Nexar's OCR engine reads the text and suggests a smart name instantly.",
        actionLabel = "Next"
    ),
    OnboardingPage(
        icon = Icons.Outlined.Hub,
        title = "Auto-Organized",
        body = "Every scan is automatically classified, tagged, and enriched with extracted amounts and dates. No manual sorting — ever.",
        actionLabel = "Next"
    ),
    OnboardingPage(
        icon = Icons.Outlined.FolderOpen,
        title = "Export Anywhere",
        body = "Save your documents to any folder on your device — iCloud Drive, Files, or external storage. Set an export folder once, then tap to export.",
        actionLabel = "Done"
    )
)

/**
 * Full-screen first-launch walkthrough.
 *
 * Shows 4 pages (Welcome → Scan → Organize → Export) with animated transitions,
 * dot-indicator progress, and a Skip button. Calls [onComplete] when the user
 * taps "Done" or "Skip".
 */
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val page = pages[currentPage]
    val isLast = currentPage == pages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Skip button
        if (!isLast) {
            TextButton(
                onClick = onComplete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 16.dp)
            ) {
                Text(
                    "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = NexarExtraTheme.colors.foregroundMuted
                )
            }
        }

        // Animated page content
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(350)) { it * dir } + fadeIn(tween(250))) togetherWith
                    (slideOutHorizontally(tween(350)) { -it * dir } + fadeOut(tween(200)))
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        ) { idx ->
            val p = pages[idx]
            PageContent(page = p)
        }

        // Bottom bar: dots + action button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 48.dp)
        ) {
            // Dot indicators
            DotIndicator(total = pages.size, current = currentPage)

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (isLast) onComplete()
                    else currentPage++
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    page.actionLabel,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon bubble
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(36.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = NexarExtraTheme.colors.foregroundSecondary,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}

@Composable
private fun DotIndicator(total: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { idx ->
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (idx == current) MaterialTheme.colorScheme.primary
                        else NexarExtraTheme.colors.borderSubtle
                    )
                    .then(
                        if (idx == current) Modifier.size(width = 20.dp, height = 8.dp)
                        else Modifier.size(8.dp)
                    )
            )
        }
    }
}
