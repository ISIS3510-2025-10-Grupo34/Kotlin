package com.tutorapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "review_drafts")
data class DraftReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tutoringSessionId: Int,
    val studentId: Int,
    val rating: Int,
    val comment: String
)

@Dao
interface DraftReviewDao {
    @Query("SELECT * FROM review_drafts")
    suspend fun getAllDrafts(): List<DraftReviewEntity>

    @Query("SELECT * FROM review_drafts WHERE tutoringSessionId = :sessionId LIMIT 1")
    suspend fun getDraftBySessionId(sessionId: Int): DraftReviewEntity?

    @Insert
    suspend fun insertDraft(draft: DraftReviewEntity)

    @Delete
    suspend fun deleteDraft(draft: DraftReviewEntity)
}


