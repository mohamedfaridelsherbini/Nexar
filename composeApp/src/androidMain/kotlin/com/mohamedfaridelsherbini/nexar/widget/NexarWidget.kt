package com.mohamedfaridelsherbini.nexar.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mohamedfaridelsherbini.nexar.MainActivity
import com.mohamedfaridelsherbini.nexar.R

class NexarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            NexarWidgetContent(context)
        }
    }

    @Composable
    private fun NexarWidgetContent(context: Context) {
        val prefs = context.getSharedPreferences("nexar_widget_prefs", Context.MODE_PRIVATE)
        val pendingCount = prefs.getInt("pending_count", 0)
        val lastScan = prefs.getString("last_scan_name", "No scans yet") ?: "No scans yet"

        GlanceTheme {
            Row(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(ImageProvider(R.drawable.widget_bg))
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = GlanceModifier.size(36.dp),
                )

                Spacer(modifier = GlanceModifier.width(10.dp))

                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "$pendingCount pending export${if (pendingCount != 1) "s" else ""}",
                        style =
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ColorProvider(day = Color.White, night = Color.White),
                            ),
                    )
                    Text(
                        text = lastScan,
                        style =
                            TextStyle(
                                fontSize = 12.sp,
                                color = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8)),
                            ),
                        maxLines = 1,
                    )
                }

                Image(
                    provider = ImageProvider(android.R.drawable.ic_menu_camera),
                    contentDescription = "Scan",
                    modifier =
                        GlanceModifier
                            .size(36.dp)
                            .clickable(actionStartActivity<MainActivity>()),
                )
            }
        }
    }
}
