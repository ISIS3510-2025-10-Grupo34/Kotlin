package com.tutorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "cached_notifications")
data class CachedNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val place: String,
    val date: String,
    val university: String
)

@Dao
interface CachedNotificationDao {
    @Query("SELECT * FROM cached_notifications WHERE university = :university ORDER BY date DESC")
    suspend fun getNotificationsByUniversity(university: String): List<CachedNotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<CachedNotificationEntity>)

    @Query("DELETE FROM cached_notifications WHERE university = :university")
    suspend fun clearForUniversity(university: String)
}