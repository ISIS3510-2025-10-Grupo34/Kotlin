package com.tutorapp.cache

import com.tutorapp.models.GamificationProfileResponse

class LruGamificationCache(private val maxSize: Int = 10) {
    private val cache = object : LinkedHashMap<Int, GamificationProfileResponse>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, GamificationProfileResponse>): Boolean {
            return size > maxSize
        }
    }

    fun get(userId: Int): GamificationProfileResponse? = cache[userId]
    fun put(userId: Int, profile: GamificationProfileResponse) { cache[userId] = profile }
}
