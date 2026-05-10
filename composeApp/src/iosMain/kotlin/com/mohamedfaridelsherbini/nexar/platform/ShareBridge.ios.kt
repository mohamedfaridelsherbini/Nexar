package com.mohamedfaridelsherbini.nexar.platform

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun sharePdf(
    pdfUri: String,
    documentName: String,
) {
    val url = NSURL.fileURLWithPath(pdfUri)
    val controller =
        UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null,
        )
    val rootVc = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootVc?.presentViewController(controller, animated = true, completion = null)
}
