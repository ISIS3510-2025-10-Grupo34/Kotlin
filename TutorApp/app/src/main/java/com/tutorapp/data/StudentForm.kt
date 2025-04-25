package  com.tutorapp.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "student_form")
data class StudentFormEntity(
    @PrimaryKey val id: Int = 1,  // Siempre uno, para reemplazar
    val name: String,
    val university: String,
    val major: String,
    val email: String,
    val password: String
)
@Dao
interface StudentFormDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveForm(form: StudentFormEntity)

    @Query("SELECT * FROM student_form LIMIT 1")
    suspend fun loadForm(): StudentFormEntity?

    @Query("DELETE FROM student_form")
    suspend fun clearForm()
}

