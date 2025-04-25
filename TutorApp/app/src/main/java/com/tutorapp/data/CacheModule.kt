package com.tutorapp.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val DATASTORE_NAME = "tutor_profile_cache"

val Context.cacheDataStore by preferencesDataStore(name = DATASTORE_NAME)

object CacheKeys {
    val PROFILE_JSON = stringPreferencesKey("profile_json")
    val INSIGHT_JSON = stringPreferencesKey("insight_json")
}