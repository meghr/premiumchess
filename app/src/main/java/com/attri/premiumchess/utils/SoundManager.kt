package com.attri.premiumchess.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var moveSoundId: Int = 0
    private var captureSoundId: Int = 0
    private var checkSoundId: Int = 0
    private var checkmateSoundId: Int = 0
    private var castleSoundId: Int = 0
    private var illegalSoundId: Int = 0
    
    private var isSoundEnabled: Boolean = true

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        // Note: Using 0 as placeholder since raw resources don't exist yet.
        // User needs to add: move.wav, capture.wav, check.wav, checkmate.wav, castle.wav, illegal.wav to res/raw/
        // To prevent crash/build error, we check for existence or just use placeholders.
        // For this implementation, we will try to load by identifier, or fail silently.
        
        moveSoundId = loadSound("move_sound")
        captureSoundId = loadSound("capture_sound")
        checkSoundId = loadSound("check_sound")
        checkmateSoundId = loadSound("checkmate_sound")
        castleSoundId = loadSound("castle_sound")
        illegalSoundId = loadSound("illegal_sound")
    }

    private fun loadSound(name: String): Int {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        return if (resId != 0) {
            soundPool?.load(context, resId, 1) ?: 0
        } else {
            0
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun playMove() {
        playSound(moveSoundId)
    }

    fun playCapture() {
        playSound(captureSoundId)
    }

    fun playCheck() {
        playSound(checkSoundId)
    }

    fun playCheckmate() {
        playSound(checkmateSoundId)
    }

    fun playCastle() {
        playSound(castleSoundId)
    }
    
    fun playIllegal() {
        playSound(illegalSoundId)
    }

    private fun playSound(soundId: Int) {
        if (!isSoundEnabled || soundId == 0) return
        soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}