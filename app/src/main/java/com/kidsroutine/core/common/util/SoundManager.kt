package com.kidsroutine.core.common.util

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.util.Log

object SoundManager {
    private var soundPool: SoundPool? = null
    private var soundIdSuccess: Int = -1
    private var soundIdTap: Int = -1
    private var soundIdStreak: Int = -1
    private var isEnabled = true
    private var appContext: Context? = null
    private const val TAG = "SoundManager"

    fun initialize(context: Context) {
        appContext = context.applicationContext
        try {
            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .build()
            // Load persisted preference
            val prefs = context.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
            isEnabled = prefs.getBoolean("sound_enabled", true)
            Log.d(TAG, "SoundPool initialized ✓ — sound ${if (isEnabled) "ON" else "OFF"}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundPool", e)
        }
    }

    /** Play success sound (game win, task complete). */
    fun playSuccess() {
        if (!isEnabled || soundPool == null) return
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                .startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 350)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play success sound", e)
        }
    }

    /** Play tap sound (button click). */
    fun playTap() {
        if (!isEnabled || soundPool == null) return
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                .startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play tap sound", e)
        }
    }

    /** Play error/wrong answer sound. */
    fun playError() {
        if (!isEnabled || soundPool == null) return
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                .startTone(ToneGenerator.TONE_CDMA_NETWORK_BUSY, 200)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play error sound", e)
        }
    }

    /** Play unlock sound (avatar item unlocked). */
    fun playUnlock() {
        if (!isEnabled || soundPool == null) return
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                .startTone(ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE, 280)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play unlock sound", e)
        }
    }

    /** Toggle sound on/off — persisted to SharedPreferences. */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        appContext?.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
            ?.edit()?.putBoolean("sound_enabled", enabled)?.apply()
        Log.d(TAG, "Sound ${if (enabled) "enabled" else "disabled"}")
    }

    fun isEnabled(): Boolean = isEnabled

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
