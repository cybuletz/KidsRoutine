package com.kidsroutine.core.common.util

import android.content.Context
import android.media.SoundPool
import android.util.Log

/**
 * Singleton sound manager for game audio.
 * Supports success, tap, and streak sounds.
 * Can be toggled on/off from settings.
 */
object SoundManager {
    private var soundPool: SoundPool? = null
    private var soundIdSuccess: Int = -1
    private var soundIdTap: Int = -1
    private var soundIdStreak: Int = -1
    private var isEnabled = true
    private const val TAG = "SoundManager"

    /**
     * Initialize sound pool. Call once from MainActivity.
     */
    fun initialize(context: Context) {
        try {
            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .build()

            // We'll use system sounds for now (future: custom audio files)
            Log.d(TAG, "SoundPool initialized ✓")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundPool", e)
        }
    }

    /**
     * Play success sound (game win, task complete).
     */
    fun playSuccess() {
        if (!isEnabled || soundPool == null) return
        try {
            // Use system success sound (beep)
            android.media.ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                100
            ).startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play success sound", e)
        }
    }

    /**
     * Play tap sound (button click).
     */
    fun playTap() {
        if (!isEnabled || soundPool == null) return
        try {
            android.media.ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                80
            ).startTone(android.media.ToneGenerator.TONE_DTMF_0, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play tap sound", e)
        }
    }

    /**
     * Play error/wrong answer sound.
     */
    fun playError() {
        if (!isEnabled || soundPool == null) return
        try {
            android.media.ToneGenerator(
                android.media.AudioManager.STREAM_NOTIFICATION,
                80
            ).startTone(android.media.ToneGenerator.TONE_CDMA_NETWORK_BUSY, 200)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play error sound", e)
        }
    }

    /**
     * Toggle sound on/off.
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        Log.d(TAG, "Sound ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Get current sound state.
     */
    fun isEnabled(): Boolean = isEnabled

    /**
     * Release resources.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
    }
}