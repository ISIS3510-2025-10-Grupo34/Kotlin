package com.tutorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Representa una sesión de tutoría en la base de datos Room.
 * Esta clase define el esquema de la tabla 'tutoring_sessions'.
 */
@Entity(tableName = "tutoring_sessions") // Define el nombre de la tabla en la BD
data class TutoringSessionEntity(
    // Clave Primaria: Identificador único de la sesión, probablemente viene del backend.
    @PrimaryKey
    val id: Int,
    // Campos correspondientes al modelo TutoringSession
    val tutor: String,
    val tutor_id: String,         // Se mantiene como String igual que en el modelo original
    val tutor_phone: String,
    val university: String,
    val course: String,
    val cost: Double,
    val date_time: String,
    val student: String?          // Nullable, indica si la sesión está libre o asignada
)


/**
 * DAO (Data Access Object) para la entidad TutoringSessionEntity.
 * Define los métodos para interactuar con la tabla 'tutoring_sessions' en la base de datos Room.
 */
@Dao // Anotación que identifica esta interfaz como un DAO para Room
interface TutoringSessionDao {

    /**
     * Obtiene todas las sesiones de tutoría disponibles (aquellas donde 'student' es NULL)
     * desde la base de datos local. Las ordena por fecha/hora descendente para mostrar
     * las más recientes o próximas primero (ajusta ORDER BY si necesitas otro orden).
     *
     * @return Una lista (List) de objetos TutoringSessionEntity.
     */
    @Query("SELECT * FROM tutoring_sessions WHERE student IS NULL ORDER BY date_time DESC")
    suspend fun getAvailableSessions(): List<TutoringSessionEntity> // suspend indica que es para coroutines


    @Query("UPDATE tutoring_sessions SET tutor_phone = :phoneNumber WHERE id = :sessionId")
    suspend fun updateTutorPhoneNumber(sessionId: Int, phoneNumber: String)

    /**
     * Inserta una lista de sesiones de tutoría en la base de datos.
     * Gracias a OnConflictStrategy.REPLACE, si una sesión con el mismo 'id'
     * (clave primaria) ya existe en la tabla, será actualizada con los nuevos datos;
     * si no existe, será insertada como una nueva fila.
     *
     * @param sessions La lista de entidades TutoringSessionEntity a insertar o reemplazar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<TutoringSessionEntity>)

    /**
     * Elimina todas las entradas (filas) de la tabla 'tutoring_sessions'.
     * Esto se usará típicamente antes de insertar un nuevo conjunto de datos
     * frescos obtenidos de la API, para asegurar que no queden datos viejos.
     */
    @Query("DELETE FROM tutoring_sessions")
    suspend fun clearAll()

    // --- Opcional ---
    // Podrías añadir más métodos si los necesitas, por ejemplo:
    // @Query("SELECT * FROM tutoring_sessions WHERE id = :sessionId LIMIT 1")
    // suspend fun getSessionById(sessionId: Int): TutoringSessionEntity?
}
