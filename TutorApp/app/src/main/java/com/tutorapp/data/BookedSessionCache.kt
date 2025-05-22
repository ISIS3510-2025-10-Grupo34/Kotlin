package com.tutorapp.data

import android.util.LruCache
import com.tutorapp.models.BookedSession
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BookedSessionCache {
    private const val CACHE_SIZE = 100 // Maximum number of entries in the cache
    private val cache = LruCache<String, List<BookedSession>>(CACHE_SIZE)

    fun put(date: LocalDate, sessions: List<BookedSession>) {
        val key = date.format(DateTimeFormatter.ISO_DATE)
        cache.put(key, sessions)
    }

    fun get(date: LocalDate): List<BookedSession>? {
        val key = date.format(DateTimeFormatter.ISO_DATE)
        return cache.get(key)
    }

    fun clear() {
        cache.evictAll()
    }
} 