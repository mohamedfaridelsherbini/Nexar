package com.mohamedfaridelsherbini.nexar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UIKit.UIWindow
import platform.Foundation.NSURL

@Composable
actual fun PreviewBridge(
    document: ScannedDocument,
    onDismiss: () -> Unit
) {
    LaunchedEffect(document) {
        val uriString = document.pdfUri ?: document.imageUris.firstOrNull()
        if (uriString != null) {
            val url = NSURL.URLWithString(uriString)
            if (url != null) {
                val activityViewController = UIActivityViewController(
                    activityItems = listOf(url),
                    applicationActivities = null
                )
                
                val rootViewController = getRootViewController()
                rootViewController?.presentViewController(activityViewController, true) {
                    onDismiss()
                }
            } else {
                onDismiss()
            }
        } else {
            onDismiss()
        }
    }
}

private fun getRootViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == platform.UIKit.UISceneActivationStateForegroundActive }
        ?.windows
        ?.filterIsInstance<UIWindow>()
        ?.firstOrNull { it.isKeyWindow() }
        ?: UIApplication.sharedApplication.keyWindow
        
    return window?.rootViewController
}
