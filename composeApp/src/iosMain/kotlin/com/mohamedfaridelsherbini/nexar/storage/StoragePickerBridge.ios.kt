package com.mohamedfaridelsherbini.nexar.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.darwin.NSObject

@Composable
actual fun StoragePickerBridge(
    onResult: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val delegate =
        remember {
            object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(
                    controller: UIDocumentPickerViewController,
                    didPickDocumentsAtURLs: List<*>,
                ) {
                    val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                    if (url != null) {
                        onResult(url.toString())
                    } else {
                        onCancel()
                    }
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    onCancel()
                }
            }
        }

    LaunchedEffect(Unit) {
        val rootViewController = getRootViewController()
        if (rootViewController != null) {
            val picker =
                UIDocumentPickerViewController(
                    forOpeningContentTypes = listOf(UTTypeFolder),
                    asCopy = false,
                )
            picker.delegate = delegate
            rootViewController.presentViewController(picker, true, null)
        } else {
            onCancel()
        }
    }
}

private fun getRootViewController(): UIViewController? {
    val window =
        UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == platform.UIKit.UISceneActivationStateForegroundActive }
            ?.windows
            ?.filterIsInstance<UIWindow>()
            ?.firstOrNull { it.isKeyWindow() }
            ?: UIApplication.sharedApplication.keyWindow

    return window?.rootViewController
}
