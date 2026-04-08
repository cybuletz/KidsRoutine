package com.kidsroutine.core.common.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Haptic Feedback Manager — provides tactile feedback on key events.
 * Complements SoundManager for a complete sensory experience.
 *
 * Usage:
 *   HapticManager.initialize(context)
 *   HapticManager.vibrateSuccess()   // XP gain, task complete
 *   HapticManager.vibrateError()     // wrong answer
 *   HapticManager.vibrateLight()     // button tap
 *   HapticManager.vibrateLevelUp()   // level up, achievement
 *   HapticManager.vibrateLootBox()   // loot box opening
 */
object HapticManager {
    private var vibrator: Vibrator? = null
    private var isEnabled = true
    private var appContext: Context? = null
    private const val TAG = "HapticManager"

    fun initialize(context: Context) {
        appContext = context.applicationContext
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        // Load persisted preference
        val prefs = context.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
        isEnabled = prefs.getBoolean("haptic_enabled", true)
        Log.d(TAG, "HapticManager initialized ✓ — haptic ${if (isEnabled) "ON" else "OFF"}")
    }

    /** Light tap — button press, card tap */
    fun vibrateLight() {
        if (!isEnabled) return
        vibrate(duration = 30, amplitude = 80)
    }

    /** Success — XP gain, task completion, correct answer */
    fun vibrateSuccess() {
        if (!isEnabled) return
        vibrate(duration = 100, amplitude = 150)
    }

    /** Error — wrong answer, mismatch */
    fun vibrateError() {
        if (!isEnabled) return
        vibratePattern(
            pattern = longArrayOf(0, 50, 50, 50),  // double-tap buzz
            amplitudes = intArrayOf(0, 120, 0, 120)
        )
    }

    /** Level up — achievement unlock, pet evolution */
    fun vibrateLevelUp() {
        if (!isEnabled) return
        vibratePattern(
            pattern = longArrayOf(0, 80, 60, 80, 60, 150),
            amplitudes = intArrayOf(0, 100, 0, 130, 0, 200)
        )
    }

    /** Loot box — escalating suspense vibration */
    fun vibrateLootBox() {
        if (!isEnabled) return
        vibratePattern(
            pattern = longArrayOf(0, 30, 30, 30, 30, 50, 30, 50, 30, 80, 30, 150),
            amplitudes = intArrayOf(0, 40, 0, 60, 0, 80, 0, 100, 0, 150, 0, 255)
        )
    }

    /** Streak milestone — celebration pattern */
    fun vibrateStreakMilestone() {
        if (!isEnabled) return
        vibratePattern(
            pattern = longArrayOf(0, 100, 50, 100, 50, 200),
            amplitudes = intArrayOf(0, 150, 0, 150, 0, 255)
        )
    }

    /** Spin wheel — tick-tick-tick pattern */
    fun vibrateSpinTick() {
        if (!isEnabled) return
        vibrate(duration = 15, amplitude = 60)
    }

    /** Boss hit — heavy impact */
    fun vibrateBossHit() {
        if (!isEnabled) return
        vibrate(duration = 150, amplitude = 200)
    }

    /** Pet interaction — gentle warmth */
    fun vibratePetInteraction() {
        if (!isEnabled) return
        vibrate(duration = 50, amplitude = 60)
    }

    /** Toggle haptic feedback on/off — persisted to SharedPreferences. */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        appContext?.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
            ?.edit()?.putBoolean("haptic_enabled", enabled)?.apply()
        Log.d(TAG, "Haptic ${if (enabled) "enabled" else "disabled"}")
    }

    fun isEnabled(): Boolean = isEnabled

    // ── Private helpers ──────────────────────────────────────────────

    private fun vibrate(duration: Long, amplitude: Int) {
        try {
            val v = vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(duration, amplitude.coerceIn(1, 255))
                v.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(duration)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed", e)
        }
    }

    private fun vibratePattern(pattern: LongArray, amplitudes: IntArray) {
        try {
            val v = vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                v.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration pattern failed", e)
        }
    }
}
