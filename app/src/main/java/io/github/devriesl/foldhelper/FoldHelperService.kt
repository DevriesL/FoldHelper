package io.github.devriesl.foldhelper

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class FoldHelperService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}