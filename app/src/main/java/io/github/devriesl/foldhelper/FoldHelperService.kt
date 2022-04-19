package io.github.devriesl.foldhelper

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent

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

    private var lastHingeAngle = 0f

    private var screenUnlock: Boolean? = null
        set(value) {
            if (field != value) {
                when(value) {
                    true -> {
                        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)
                        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                        foldingMode = calcFoldingMode()
                    }
                    false -> {
                        sensorManager.unregisterListener(sensorEventListener)
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
                    screenUnlock = false
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
    }

    internal enum class FoldingMode {
        UNKNOWN, PHONE_MODE, TABLET_MODE
    }

    companion object {
        private const val TAG = "FoldHelperService"
        private const val ASPECT_RATIO_16_10 = 1.6f
    }
}