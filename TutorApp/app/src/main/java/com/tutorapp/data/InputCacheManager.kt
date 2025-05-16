package com.tutorapp.data

import androidx.collection.LruCache

/**
 * Singleton que mantiene un cache LRU de claves y valores String.
 * Máximo de 20 entradas (puedes ajustarlo).
 */
object InputCacheManager {
    private const val MAX_ENTRIES = 20
    private val cache = object : LruCache<String, String>(MAX_ENTRIES) {}

    fun put(key: String, value: String) {
        cache.put(key, value)
    }

    fun get(key: String): String? {
        return cache.get(key)
    }

    fun clear() {
        cache.evictAll()
    }
}
