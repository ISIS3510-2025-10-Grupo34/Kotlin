package com.tutorapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "gamification_cache")
data class GamificationCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val points: Int?,
    val achievement: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface GamificationCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GamificationCacheEntity)

    @Query("SELECT * FROM gamification_cache")
    suspend fun getAll(): List<GamificationCacheEntity>

    @Delete
    suspend fun delete(entry: GamificationCacheEntity)
}