package  com.tutorapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "tutor_form")
data class TutorFormEntity(
    @PrimaryKey val id: Int = 1,  // Siempre uno, para reemplazar
    val name: String,
    val university: String,
    val expertise: String,
    val email: String,
    val password: String,
    val phoneNumber: String
)
@Dao
interface TutorFormDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveForm(form: TutorFormEntity)

    @Query("SELECT * FROM tutor_form LIMIT 1")
    suspend fun loadForm(): TutorFormEntity?

    @Query("DELETE FROM tutor_form")
    suspend fun clearForm()
}