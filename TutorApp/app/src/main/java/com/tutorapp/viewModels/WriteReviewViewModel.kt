package com.tutorapp.viewModels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.data.AppDatabase
import com.tutorapp.data.DraftReviewEntity
import com.tutorapp.models.PostReviewRequest
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WriteReviewViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).draftReviewDao()

    // 1) SharedFlow para notificar UI cuando se envían borradores
    private val _sentCount = MutableSharedFlow<Int>(replay = 0)
    val sentCount = _sentCount.asSharedFlow()

    /**
     * Intenta enviar al servidor; si falla o no hay internet, guarda un borrador.
     * onResult(sent:Boolean, message:String)
     */
    fun postReviewOrSaveDraft(
        tutoringSessionId: Int,
        studentId: Int,
        rating: Int,
        comment: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            // 2) Primero verificamos duplicado local
            val existing = dao.getDraftBySessionId(tutoringSessionId)
            if (existing != null) {
                onResult(false, "A review for this session is already pending submission.")
                return@launch
            }

            // 3) Intentamos enviar en línea
            if (isInternetAvailable()) {
                try {
                    val body = PostReviewRequest(
                        tutoringSessionId, studentId, rating, comment
                    )
                    val resp = RetrofitClient.instance.postReview(body)
                    if (resp.isSuccessful) {
                        onResult(true, resp.body()?.message ?: "Review sent successfully.")
                        return@launch
                    }
                } catch (_: Exception) { /* falla envío */ }
            }

            // 4) Si llegamos aquí, guardamos borrador
            dao.insertDraft(
                DraftReviewEntity(
                    tutoringSessionId = tutoringSessionId,
                    studentId = studentId,
                    rating = rating,
                    comment = comment
                )
            )
            onResult(false, "No connection – your review will be sent when online.")
        }
    }

    /**
     * Envía todos los borradores pendientes cuando hay internet,
     * y emite un solo toast indicando cuántos se enviaron.
     */
    fun sendPendingReviews() {
        viewModelScope.launch {
            if (!isInternetAvailable()) return@launch
            val drafts = withContext(kotlinx.coroutines.Dispatchers.IO) {
                dao.getAllDrafts()
            }

            var sent = 0
            drafts.forEach { draft ->
                try {
                    val body = PostReviewRequest(
                        draft.tutoringSessionId,
                        draft.studentId,
                        draft.rating,
                        draft.comment
                    )
                    val resp = RetrofitClient.instance.postReview(body)
                    if (resp.isSuccessful) {
                        withContext(kotlinx.coroutines.Dispatchers.IO) {
                            dao.deleteDraft(draft)
                        }
                        sent++
                    }
                } catch (_: Exception) { /* seguirá pendiente */ }
            }

            if (sent > 0) {
                _sentCount.emit(sent)
            }
        }
    }

    @Suppress("ServiceCast")
    fun isInternetAvailable(): Boolean {
        val cm = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.activeNetwork?.let { nw ->
            cm.getNetworkCapabilities(nw)?.let { caps ->
                return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        return false
    }
}
