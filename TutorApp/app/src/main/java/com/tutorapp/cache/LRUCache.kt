package com.tutorapp.cache

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class LRUCache<K, V>(private val capacity: Int) {
    private val cache = ConcurrentHashMap<K, V>()
    private val size = AtomicInteger(0)
    private val accessOrder = mutableListOf<K>()
    private val lock = Any()

    fun get(key: K): V? {
        return cache[key]?.also {
            synchronized(lock) {
                accessOrder.remove(key)
                accessOrder.add(key)
            }
        }
    }

    fun put(key: K, value: V) {
        synchronized(lock) {
            if (cache.containsKey(key)) {
                accessOrder.remove(key)
            } else if (size.get() >= capacity) {
                val eldestKey = accessOrder.removeFirst()
                cache.remove(eldestKey)
                size.decrementAndGet()
            }
            cache[key] = value
            accessOrder.add(key)
            size.incrementAndGet()
        }
    }

    fun clear() {
        synchronized(lock) {
            cache.clear()
            accessOrder.clear()
            size.set(0)
        }
    }

    fun size(): Int = size.get()
}