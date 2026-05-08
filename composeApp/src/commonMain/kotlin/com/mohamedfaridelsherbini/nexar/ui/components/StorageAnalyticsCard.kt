package com.mohamedfaridelsherbini.nexar.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.usecase.StorageAnalytics
import com.mohamedfaridelsherbini.nexar.ui.theme.NexarExtraTheme

internal fun Long.toReadableSize(): String = when {
    this >= 1_073_741_824L -> "${(this / 1_073_741_824.0).round1dp()} GB"
    this >= 1_048_576L -> "${(this / 1_048_576.0).round1dp()} MB"
    this >= 1_024L -> "${(this / 1_024.0).toLong()} KB"
    else -> "$this B"
}

private fun Double.round1dp(): String {
    val scaled = kotlin.math.round(this * 10).toLong()
    return "${scaled / 10}.${scaled % 10}"
}

private val categoryColors = mapOf(
    DocumentCategory.Receipt to Color(0xFFF59E0B),
    DocumentCategory.Invoice to Color(0xFF06B6D4),
    DocumentCategory.IdDocument to Color(0xFF10B981),
    DocumentCategory.Contract to Color(0xFF8B5CF6),
    DocumentCategory.Medical to Color(0xFFEF4444),
    DocumentCategory.Other to Color(0xFF94A3B8)
)

@Composable
fun StorageAnalyticsCard(analytics: StorageAnalytics) {
    if (analytics.totalDocuments == 0) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, NexarExtraTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Storage overview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = analytics.totalBytes.toReadableSize(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = NexarExtraTheme.colors.foregroundSecondary
            )
        }

        // Segmented bar
        if (analytics.totalBytes > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                DocumentCategory.entries.forEach { cat ->
                    val bytes = analytics.perCategory[cat] ?: 0L
                    if (bytes > 0) {
                        val fraction = bytes.toFloat() / analytics.totalBytes.toFloat()
                        val animated by animateFloatAsState(
                            targetValue = fraction,
                            animationSpec = tween(600),
                            label = "bar_$cat"
                        )
                        Box(
                            modifier = Modifier
                                .weight(animated.coerceAtLeast(0.001f))
                                .fillMaxHeight()
                                .background(categoryColors[cat] ?: Color.Gray)
                        )
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DocumentCategory.entries.forEach { cat ->
                val bytes = analytics.perCategory[cat] ?: 0L
                if (bytes > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(categoryColors[cat] ?: Color.Gray)
                        )
                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = NexarExtraTheme.colors.foregroundSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(label = "Total", value = analytics.totalDocuments.toString())
            StatItem(label = "Exported", value = analytics.exportedDocuments.toString())
            StatItem(
                label = "Pending",
                value = (analytics.totalDocuments - analytics.exportedDocuments).toString()
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NexarExtraTheme.colors.foregroundMuted
        )
    }
}
