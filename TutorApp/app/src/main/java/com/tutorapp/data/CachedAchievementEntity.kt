package com.tutorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_achievements")
data class CachedAchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val achievementName: String,
    val timestamp: Long // Se puede usar para orden o sincronización futura
)