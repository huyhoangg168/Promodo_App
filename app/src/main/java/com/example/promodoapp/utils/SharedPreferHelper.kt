package com.example.promodoapp.utils

import android.content.Context
import android.net.Uri

object SharedPrefHelper {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_AVATAR_URI = "avatar_uri"

    fun saveAvatarUri(context: Context, uri: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AVATAR_URI, uri).apply()
    }

    fun getAvatarUri(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_AVATAR_URI, null)
    }
}
