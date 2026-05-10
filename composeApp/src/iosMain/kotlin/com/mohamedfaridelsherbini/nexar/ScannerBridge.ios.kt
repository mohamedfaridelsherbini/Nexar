package com.mohamedfaridelsherbini.nexar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.VisionKit.VNDocumentCameraScan
import platform.VisionKit.VNDocumentCameraViewController
import platform.VisionKit.VNDocumentCameraViewControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.time
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ScannerBridge(
    onResult: (ScannedDocument) -> Unit,
    onCancel: () -> Unit,
) {
    val delegate =
        remember {
            object : NSObject(), VNDocumentCameraViewControllerDelegateProtocol {
                override fun documentCameraViewController(
                    controller: VNDocumentCameraViewController,
                    didFinishWithScan: VNDocumentCameraScan,
                ) {
                    val documentId = NSUUID().UUIDString()
                    val imageUris = mutableListOf<String>()

                    for (i in 0 until didFinishWithScan.pageCount.toInt()) {
                        val image = didFinishWithScan.imageOfPageAtIndex(i.toULong())
                        val data = UIImageJPEGRepresentation(image, 0.8)
                        if (data != null) {
                            val fileName = "scan_${documentId}_page_$i.jpg"
                            val fileUrl = NSURL.fileURLWithPath(NSTemporaryDirectory() + fileName)
                            data.writeToURL(fileUrl, true)
                            imageUris.add(fileUrl.toString())
                        }
                    }

                    val formatter =
                        NSDateFormatter().apply {
                            setDateFormat("yyyy-MM-dd HH:mm")
                        }
                    val dateString = formatter.stringFromDate(NSDate())

                    val document =
                        ScannedDocument(
                            id = documentId,
                            name = "Scan $dateString",
                            dateMillis = time(null) * 1000,
                            imageUris = imageUris,
                            pdfUri = null,
                        )

                    controller.dismissViewControllerAnimated(true) {
                        onResult(document)
                    }
                }

                override fun documentCameraViewControllerDidCancel(controller: VNDocumentCameraViewController) {
                    controller.dismissViewControllerAnimated(true) {
                        onCancel()
                    }
                }

                override fun documentCameraViewController(
                    controller: VNDocumentCameraViewController,
                    didFailWithError: platform.Foundation.NSError,
                ) {
                    controller.dismissViewControllerAnimated(true) {
                        onCancel()
                    }
                }
            }
        }

    LaunchedEffect(Unit) {
        val rootViewController = getRootViewController()
        if (rootViewController != null) {
            val scannerViewController = VNDocumentCameraViewController()
            scannerViewController.delegate = delegate
            rootViewController.presentViewController(scannerViewController, true, null)
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
