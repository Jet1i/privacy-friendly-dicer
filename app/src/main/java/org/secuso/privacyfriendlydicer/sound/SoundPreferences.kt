package org.secuso.privacyfriendlydicer.sound

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri

object SoundPreferences {
    const val KEY_SOUND_ENABLED = "pref_sound_enabled"
    const val KEY_CUSTOM_SOUND_URI = "pref_custom_sound_uri"
    private const val PREF_FILE = "custom_sound_prefs"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    fun isSoundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SOUND_ENABLED, true)

    fun getCustomSoundUri(context: Context): Uri? =
        prefs(context).getString(KEY_CUSTOM_SOUND_URI, null)?.let(Uri::parse)

    fun setCustomSoundUri(context: Context, uri: Uri?, contentResolver: ContentResolver) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        getCustomSoundUri(context)?.takeIf { ContentResolver.SCHEME_CONTENT == it.scheme }?.let { previous ->
            runCatching { contentResolver.releasePersistableUriPermission(previous, flags) }
        }

        if (uri == null) {
            prefs(context).edit().remove(KEY_CUSTOM_SOUND_URI).apply()
            return
        }

        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            runCatching { contentResolver.takePersistableUriPermission(uri, flags) }
        }
        prefs(context).edit().putString(KEY_CUSTOM_SOUND_URI, uri.toString()).apply()
    }
}
