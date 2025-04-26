package com.tutorapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "session_data")
data class SessionDataEntity(
    @PrimaryKey val id: Int = 1,  // Siempre uno, para reemplazar
    val token: String,

)
@Dao
interface SessionDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveData(data: SessionDataEntity)

    @Query("SELECT * FROM session_data LIMIT 1")
    suspend fun loadData(): SessionDataEntity?

    @Query("DELETE FROM session_data")
    suspend fun clearData()
}