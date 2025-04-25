// TutorProfileViewModel.kt
package com.tutorapp.viewModels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.tutorapp.data.CacheKeys
import com.tutorapp.data.cacheDataStore
import com.tutorapp.models.GetTutorProfileResponse
import com.tutorapp.models.GetTimeToBookInsightResponse
import com.tutorapp.remote.RetrofitClient
import com.tutorapp.views.TutorProfileUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TutorProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = app.cacheDataStore
    private val gson = Gson()

    private val _uiState = MutableStateFlow(TutorProfileUiState())
    val uiState: StateFlow<TutorProfileUiState> = _uiState.asStateFlow()

    fun loadTutorProfile(tutorId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1) Leer cache persistente
            val prefs = dataStore.data.first()
            prefs[CacheKeys.PROFILE_JSON]?.let { json ->
                val p = gson.fromJson(json, GetTutorProfileResponse::class.java)
                _uiState.update { state -> state.copy(profile = p) }
            }
            prefs[CacheKeys.INSIGHT_JSON]?.let { json ->
                val i = gson.fromJson(json, GetTimeToBookInsightResponse::class.java)
                _uiState.update { state -> state.copy(insight = i) }
            }

            // 2) Refrescar desde red si internet disponible
            if (isInternetAvailable()) {
                try {
                    val respP = RetrofitClient.instance.getTutorProfile(tutorId)
                    val respI = RetrofitClient.instance.getTimeToBookInsight(tutorId)
                    if (respP.isSuccessful && respI.isSuccessful) {
                        val perfil = respP.body()!!
                        val insight = respI.body()!!
                        // Guardar cache
                        dataStore.edit { ds ->
                            ds[CacheKeys.PROFILE_JSON] = gson.toJson(perfil)
                            ds[CacheKeys.INSIGHT_JSON] = gson.toJson(insight)
                        }
                        _uiState.update { state ->
                            state.copy(
                                profile = perfil,
                                insight = insight,
                                isLoading = false,
                                isStale = false,
                                error = null
                            )
                        }
                    } else {
                        _uiState.update { state -> state.copy(isLoading = false, error = "Error al actualizar del servidor") }
                    }
                } catch (e: Exception) {
                    _uiState.update { state -> state.copy(isLoading = false, isStale = true) }
                }
            } else {
                _uiState.update { state -> state.copy(isLoading = false, isStale = true) }
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun isInternetAvailable(): Boolean {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.activeNetwork?.let { nw ->
            cm.getNetworkCapabilities(nw)?.let { caps ->
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return true
            }
        }
        return false
    }
}
