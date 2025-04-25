package  com.tutorapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "tutor_profile")
data class TutorProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val university: String,
    val ratings: Double,
    val whatsappContact: String,
    val subjects: String
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tutorId: Int = 1,
    val rating: Int,
    val comment: String
)

@Entity(tableName = "insight")
data class InsightEntity(
    @PrimaryKey val id: Int = 1,
    val message: String,
    val time: Int
)

@Dao
interface TutorProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTutorProfile(profile: TutorProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReviews(reviews: List<ReviewEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInsight(insight: InsightEntity)

    @Query("SELECT * FROM tutor_profile LIMIT 1")
    suspend fun loadTutorProfile(): TutorProfileEntity?

    @Query("SELECT * FROM reviews WHERE tutorId = :tutorId")
    suspend fun loadReviews(tutorId: Int = 1): List<ReviewEntity>

    @Query("SELECT * FROM insight LIMIT 1")
    suspend fun loadInsight(): InsightEntity?
}

