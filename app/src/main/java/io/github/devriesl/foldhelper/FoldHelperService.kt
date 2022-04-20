package io.github.devriesl.foldhelper

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import io.github.devriesl.foldhelper.Constants.APP_BLACK_LIST
import kotlin.math.abs

class FoldHelperService : AccessibilityService() {
    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val windowManager: WindowManager by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }

    private val keyguardManager: KeyguardManager by lazy {
        getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    private var lastHingeAngle: Float? = null

    private var screenUnlock: Boolean? = null
        set(value) {
            if (field != value) {
                when(value) {
                    true -> {
                        Log.d(TAG, "Screen Unlock")
                        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)
                        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                        foldingMode = calcFoldingMode()
                    }
                    false -> {
                        Log.d(TAG, "Screen Lock")
                        sensorManager.unregisterListener(sensorEventListener)
                        lastHingeAngle = null
                        foldingMode = FoldingMode.UNKNOWN
                    }
                    else -> {
                    }
                }
            }
            field = value
        }

    private var foldingMode: FoldingMode = FoldingMode.UNKNOWN
        set(value) {
            if (field != value) { handleFoldingEvent(field, value) }
            field = value
        }

    private var phoneModeApp: String? = null

    private var tabletModeApp: String? = null

    private var currentApp: String? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent?) {
            sensorEvent?.values?.getOrNull(0)?.let { lastHingeAngle = it }
            foldingMode = calcFoldingMode()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    private val lockScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> {
                    screenUnlock = true
                }
                Intent.ACTION_SCREEN_OFF -> {
                    if (keyguardManager.isKeyguardLocked) {
                        screenUnlock = false
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    if (!keyguardManager.isKeyguardLocked) {
                        screenUnlock = true
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        screenUnlock = !keyguardManager.isKeyguardLocked
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(lockScreenReceiver, intentFilter)
    }

    override fun onDestroy() {
        unregisterReceiver(lockScreenReceiver)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == TYPE_WINDOW_STATE_CHANGED && validatePackageName(event.packageName)) {
            val rect = Rect()
            event.source?.getBoundsInScreen(rect)
            if (rect.width() > FOREGROUND_MIN_SIZE && rect.height() > FOREGROUND_MIN_SIZE) {
                handleAppSwitchEvent(event.packageName.toString())
            }
        }
    }

    override fun onInterrupt() {
    }

    private fun calcFoldingMode(): FoldingMode {
        val width = windowManager.currentWindowMetrics.bounds.width().toFloat()
        val height = windowManager.currentWindowMetrics.bounds.height().toFloat()
        val aspectRatio = if (width > height) { width / height } else { height / width }

        return if (aspectRatio < ASPECT_RATIO_16_10) {
            FoldingMode.TABLET_MODE
        } else {
            FoldingMode.PHONE_MODE
        }
    }

    private fun handleFoldingEvent(previousMode: FoldingMode, currentMode: FoldingMode) {
        Log.d(TAG, "handleFoldingEvent: Previous $previousMode, Current $currentMode")
        if (currentMode == FoldingMode.TABLET_MODE) {
            tabletModeApp?.let { launchApplication(it) }
        } else if (currentMode == FoldingMode.PHONE_MODE) {
            phoneModeApp?.let { launchApplication(it) }
        }
    }

    private fun handleAppSwitchEvent(packageName: String) {
        Log.d(TAG, "handleAppSwitchEvent: PackageName $packageName, HingeAngle $lastHingeAngle, FoldingMode $foldingMode")
        currentApp = packageName
        lastHingeAngle?.let {
            if (isDefiniteState(it)) {
                when(foldingMode) {
                    FoldingMode.PHONE_MODE -> phoneModeApp = packageName
                    FoldingMode.TABLET_MODE -> tabletModeApp = packageName
                    else -> {}
                }
            }
        }
    }

    private fun isDefiniteState(hingeAngle: Float): Boolean {
        return when {
            abs(hingeAngle) < DEFINITE_STATE_MAX_ANGLE ||
            abs(hingeAngle - 180) < DEFINITE_STATE_MAX_ANGLE -> {
                true
            }
            else -> false
        }
    }

    private fun launchApplication(packageName: String) {
        if (packageName == currentApp) {
            Log.d(TAG, "launchApplication: Skip current $packageName")
        } else {
            Log.d(TAG, "launchApplication: $packageName")
            packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
                startActivity(intent)
            }
        }
    }

    private fun validatePackageName(charSequence: CharSequence?): Boolean {
        var valid = true

        if (charSequence.isNullOrEmpty()) {
            valid = false
        } else {
            APP_BLACK_LIST.forEach {
                if (charSequence.contains(it)) {
                    valid = false
                    Log.d(TAG, "validatePackageName: Ignored $charSequence")
                }
            }
        }

        return valid
    }

    internal enum class FoldingMode {
        UNKNOWN, PHONE_MODE, TABLET_MODE
    }

    companion object {
        private const val TAG = "FoldHelperService"
        private const val ASPECT_RATIO_16_10 = 1.6f
        private const val FOREGROUND_MIN_SIZE = 480
        private const val DEFINITE_STATE_MAX_ANGLE = 10f
    }
}