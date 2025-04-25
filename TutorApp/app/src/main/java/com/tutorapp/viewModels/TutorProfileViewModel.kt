package com.tutorapp.viewModels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.*
import com.tutorapp.data.*
import com.tutorapp.models.*
import com.tutorapp.remote.RetrofitClient
import com.tutorapp.views.TutorProfileUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TutorProfileViewModel(
    app: Application,
    private val dao: TutorProfileDao
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(TutorProfileUiState())
    val uiState: StateFlow<TutorProfileUiState> = _uiState.asStateFlow()

    fun loadTutorProfile(tutorId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // ==== 1) Cache-first: carga desde Room ====
            dao.loadTutorProfile()?.let { e ->
                val reviews = dao.loadReviews(tutorId).map { Review(it.rating, it.comment) }
                _uiState.update { state ->
                    state.copy(
                        profile = GetTutorProfileResponse(
                            data = TutorData(
                                name = e.name,
                                university = e.university,
                                ratings = e.ratings,
                                reviews = reviews,
                                whatsappContact = e.whatsappContact,
                                subjects = e.subjects
                            )
                        )
                    )
                }
            }
            dao.loadInsight()?.let { ie ->
                _uiState.update { state ->
                    state.copy(
                        insight = GetTimeToBookInsightResponse(ie.message, ie.time)
                    )
                }
            }

            // ==== 2) Si hay internet, refresca desde API y sobreescribe Room ====
            if (isInternetAvailable()) {
                try {
                    val respP = RetrofitClient.instance.getTutorProfile(tutorId)
                    val respI = RetrofitClient.instance.getTimeToBookInsight(tutorId)
                    if (respP.isSuccessful && respI.isSuccessful) {
                        val perfil = respP.body()!!
                        val insight = respI.body()!!

                        dao.clearReviews(tutorId)

                        // guarda en Room
                        dao.saveTutorProfile(
                            TutorProfileEntity(
                                id = 1,
                                name = perfil.data.name,
                                university = perfil.data.university,
                                ratings = perfil.data.ratings,
                                whatsappContact = perfil.data.whatsappContact,
                                subjects = perfil.data.subjects
                            )
                        )
                        dao.saveReviews(perfil.data.reviews.map {
                            ReviewEntity(tutorId = tutorId, rating = it.rating, comment = it.comment)
                        })
                        dao.saveInsight(
                            InsightEntity(id = 1, message = insight.message, time = insight.time)
                        )

                        // actualiza estado UI
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
                        _uiState.update { it.copy(isLoading = false, error = "Error al actualizar del servidor") }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, isStale = true) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, isStale = true) }
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val cm = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.activeNetwork?.let { nw ->
            cm.getNetworkCapabilities(nw)?.let { caps ->
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return true
            }
        }
        return false
    }
}
