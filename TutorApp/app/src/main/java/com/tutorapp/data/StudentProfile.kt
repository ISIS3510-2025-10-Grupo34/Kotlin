package com.tutorapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val id: Int = 1,  // Siempre uno, para reemplazar
    val name: String,
    val university: String,
    val major: String,
    val learningStyles: String,

    )
@Dao
interface StudentProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveData(data: StudentProfileEntity)

    @Query("SELECT * FROM student_profile LIMIT 1")
    suspend fun loadData(): StudentProfileEntity?

    @Query("DELETE FROM student_profile")
    suspend fun clearData()
}