package com.tutorapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import com.tutorapp.views.CourseSimple
import com.tutorapp.views.UniversitySimple

@Entity(tableName = "tutoring_session_drafts")
data class TutoringSessionDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tutorId: String,
    val courseId: String,
    val cost: String,
    val dateTime: String
)

@Dao
interface TutoringSessionDraftDao {
    @Query("SELECT * FROM tutoring_session_drafts")
    suspend fun getAll(): List<TutoringSessionDraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: TutoringSessionDraftEntity)

    @Delete
    suspend fun delete(draft: TutoringSessionDraftEntity)
}

@Entity(tableName="search_cache")
data class SearchCacheEntity(
    @PrimaryKey val id: Int,
    val unis: List<UniversitySimple>,
    val map: Map<String,List<CourseSimple>>
)
@Dao interface SearchCacheDao {
    @Query("SELECT * FROM search_cache WHERE id=1") fun load(): SearchCacheEntity?
    @Insert(onConflict=REPLACE) fun insert(e:SearchCacheEntity)
    @Query("DELETE FROM search_cache") fun clear()
}
