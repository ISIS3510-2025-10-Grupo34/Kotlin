package com.tutorapp.data // O el paquete donde prefieras poner las Factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tutorapp.viewModels.ShowTutorsViewModel // Importa tu ViewModel

/**
 * ViewModelProvider.Factory para crear instancias de ShowTutorsViewModel.
 * Se encarga de proporcionar las dependencias necesarias (Application y TutoringSessionDao)
 * al constructor del ViewModel.
 */
class ShowTutorsViewModelFactory(
    private val application: Application // Necesita Application para obtener la BD
) : ViewModelProvider.Factory {

    /**
     * Este método es llamado por el sistema Android cuando necesita crear
     * una instancia del ViewModel asociado a esta Factory.
     * @param modelClass La clase del ViewModel que se solicita crear.
     * @return Una instancia del ViewModel solicitado (T).
     * @throws IllegalArgumentException si la clase solicitada no es ShowTutorsViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Comprueba si la clase ViewModel solicitada es ShowTutorsViewModel
        if (modelClass.isAssignableFrom(ShowTutorsViewModel::class.java)) {

            // Si coincide, obtiene la instancia del DAO necesario desde AppDatabase
            val tutoringSessionDao = AppDatabase.getDatabase(application).tutoringSessionDao()

            // Crea y devuelve una nueva instancia de ShowTutorsViewModel,
            // pasando las dependencias requeridas (application y el dao)
            @Suppress("UNCHECKED_CAST") // Es seguro aquí porque ya comprobamos la clase
            return ShowTutorsViewModel(application, tutoringSessionDao) as T
        }
        // Si se pide crear un ViewModel diferente, lanza una excepción
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}