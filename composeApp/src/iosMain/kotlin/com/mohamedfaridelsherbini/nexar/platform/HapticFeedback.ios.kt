package com.mohamedfaridelsherbini.nexar.platform

import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual fun triggerSuccessHaptic() {
    val generator = UINotificationFeedbackGenerator()
    generator.prepare()
    generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
}

actual fun triggerWarningHaptic() {
    val generator = UINotificationFeedbackGenerator()
    generator.prepare()
    generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeWarning)
}
