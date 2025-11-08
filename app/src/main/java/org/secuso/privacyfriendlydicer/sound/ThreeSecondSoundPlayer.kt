package org.secuso.privacyfriendlydicer.sound

import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper

/**
 * Plays an audio Uri for at most three seconds while handling audio focus and cleanup.
 */
class ThreeSecondSoundPlayer(context: Context) {

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            stop()
        }
    }

    private val stopRunnable = Runnable { stop() }

    private var focusRequest: AudioFocusRequest? = null
    private var mediaPlayer: MediaPlayer? = null

    @Synchronized
    fun play(uri: Uri) {
        stopLocked()
        val player = MediaPlayer()
        mediaPlayer = player
        try {
            player.setAudioAttributes(audioAttributes)
            val resId = uri.toRawResourceId(appContext)
            if (resId != null) {
                appContext.resources.openRawResourceFd(resId)?.use { afd ->
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                } ?: throw IllegalStateException("Cannot open resource $resId")
            } else {
                player.setDataSource(appContext, uri)
            }
            player.setOnPreparedListener {
                it.start()
                handler.postDelayed(stopRunnable, MAX_DURATION_MS)
            }
            player.setOnCompletionListener { stop() }
            player.setOnErrorListener { _, _, _ ->
                stop()
                true
            }
            if (!requestFocus()) {
                stopLocked()
                return
            }
            player.prepareAsync()
        } catch (_: Exception) {
            stopLocked()
        }
    }

    @Synchronized
    fun stop() {
        stopLocked()
    }

    private fun stopLocked() {
        handler.removeCallbacks(stopRunnable)
        mediaPlayer?.let { player ->
            runCatching { player.setOnCompletionListener(null) }
            runCatching { player.setOnErrorListener(null) }
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
            runCatching { player.release() }
        }
        mediaPlayer = null
        abandonFocus()
    }

    private fun requestFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(focusChangeListener)
        }
        focusRequest = null
    }

    companion object {
        private const val MAX_DURATION_MS = 3_000L
    }
}

private fun Uri.toRawResourceId(context: Context): Int? {
    if (scheme != ContentResolver.SCHEME_ANDROID_RESOURCE) return null
    val authority = authority ?: context.packageName
    return when (pathSegments.size) {
        1 -> pathSegments[0].toIntOrNull()
        2 -> {
            val (type, name) = pathSegments
            context.resources.getIdentifier(name, type, authority).takeIf { it != 0 }
        }
        else -> null
    }
}
