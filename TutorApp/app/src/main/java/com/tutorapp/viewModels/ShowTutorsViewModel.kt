package com.tutorapp.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.cache.LRUCache
import com.tutorapp.models.PostFilterCounterIncreaseRequest
import com.tutorapp.models.PostTimeToBookRequest
import com.tutorapp.models.SearchResultFilterResponse
import com.tutorapp.models.TutoringSession
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowTutorsViewModel : ViewModel() {

    private val cacheCapacity = 10
    private val tutorCardCache = LRUCache<Int, TutoringSession>(cacheCapacity)

    private val _cachedSessions = MutableStateFlow<List<TutoringSession>>(emptyList())
    val cachedSessions: StateFlow<List<TutoringSession>> = _cachedSessions

    var sessions by mutableStateOf<List<TutoringSession>>(emptyList())
        private set

    var emptyFilter by mutableStateOf(false)

    init {
        _cachedSessions.value = tutorCardCache.values.toList()
        sessions = tutorCardCache.values.toList()
    }

    fun loadInitialSessions(){
        viewModelScope.launch{
            _cachedSessions.value = tutorCardCache.values.toList()
            sessions = tutorCardCache.values.toList()

            try{
                val response = RetrofitClient.instance.tutoringSessions()
                if(response.isSuccessful){
                    val fetchedSessions = response.body()?.filter{it.student == null } ?: emptyList()

                    tutorCardCache.clear()
                    fetchedSessions.forEach{ session ->
                        tutorCardCache[session.id] = session
                    }
                    _cachedSessions.value = tutorCardCache.values.toList()
                    sessions = tutorCardCache.values.toList()
                }else{
                    println("Error al cargar sesiones iniciales: ${response.code()}")
                }
            }catch (e:Exception){
                println("Excepción al cargar sesiones iniciales: ${e.message}")
            }
        }
    }

    fun getAllSessions(onComplete: (List<TutoringSession>?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.tutoringSessions()

                if (response.isSuccessful) {
                    sessions = response.body()?.filter { it.student == null } ?: emptyList()
                    withContext(Dispatchers.Main) {
                        onComplete(sessions)
                    }
                } else {
                    println("Error: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        onComplete(null)
                    }
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }

    suspend fun onFilterClick(university: String, course: String, professor: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.tutoringSessions()
                val allSessions = response.body()?.filter { it.student == null } ?: emptyList()
                val filteredList = mutableListOf<TutoringSession>()

                if (university.isNotEmpty() && professor.isNotEmpty() && course.isNotEmpty()) {
                    filteredList.addAll(allSessions.filter {
                        it.tutor.contains(professor, ignoreCase = true) &&
                                it.course.contains(course, ignoreCase = true) &&
                                it.university.contains(university, ignoreCase = true)
                    })
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
                } else if (university.isNotEmpty() && course.isNotEmpty()) {
                    filteredList.addAll(allSessions.filter {
                        it.course.contains(course, ignoreCase = true) &&
                                it.university.contains(university, ignoreCase = true)
                    })
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
                } else if (university.isNotEmpty() && professor.isNotEmpty()) {
                    filteredList.addAll(allSessions.filter {
                        it.tutor.contains(professor, ignoreCase = true) &&
                                it.university.contains(university, ignoreCase = true)
                    })
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
                } else if (course.isNotEmpty() && professor.isNotEmpty()) {
                    filteredList.addAll(allSessions.filter {
                        it.tutor.contains(professor, ignoreCase = true) &&
                                it.course.contains(course, ignoreCase = true)
                    })
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
                } else if (university.isNotEmpty()) {
                    filteredList.addAll(allSessions.filter {
                        it.university.contains(university, ignoreCase = true)
                    })
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
                } else if (professor.isNotEmpty()) {
                    filteredList.addAll(allSessions.filter {
                        it.tutor.contains(professor, ignoreCase = true)
                    })
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
                } else if (course.isNotEmpty()) {
                    filteredList.addAll(allSessions.filter {
                        it.course.contains(course, ignoreCase = true)
                    })
                    RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
                } else {
                    sessions = tutorCardCache.values.toList()
                    emptyFilter = false
                    return@launch
                }

                if (filteredList.isEmpty()) {
                    emptyFilter = true
                    sessions = tutorCardCache.values.toList()
                } else {
                    emptyFilter = false
                    sessions = filteredList
                    /*emptyFilter = false
                    sessions = filteredList

                    tutorCardCache.clear()
                    filteredList.forEach { session ->
                        tutorCardCache[session.id] = session
                    }
                    _cachedSessions.value = tutorCardCache.values.toList()*/
                }

            } catch (e: Exception) {
                println("Error al aplicar filtros: ${e.message}")
                // En caso de error, mostrar lo que esté en caché
                sessions = tutorCardCache.values.toList()
                emptyFilter = false
            }
        }
    }

    /*suspend fun onFilterClick(university:String, course:String, professor:String){
        val response = RetrofitClient.instance.tutoringSessions()
        if(university.isNotEmpty()  && professor.isNotEmpty() && course.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.tutor.contains(professor, ignoreCase = true) && element.course.contains(course, ignoreCase = true) && element.university.contains(university, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
            if(filteredList.size == 0){
                emptyFilter = true
                getAllSessions {  }
            }
            else{
                emptyFilter = false
                sessions = filteredList
            }
        }
        else if(university.isNotEmpty() && course.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.course.contains(course, ignoreCase = true) && element.university.contains(university, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
            if(filteredList.size == 0){
                emptyFilter = true
                getAllSessions {  }
            }
            else{
                emptyFilter = false
                sessions = filteredList
            }
        }
        else if(university.isNotEmpty() && professor.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.course.contains(professor, ignoreCase = true) && element.university.contains(university, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
            if(filteredList.size == 0){
                emptyFilter = true
                getAllSessions {  }
            }
            else{
                emptyFilter = false
                sessions = filteredList
            }
        }
        else if(course.isNotEmpty() && professor.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.course.contains(professor, ignoreCase = true) && element.tutor.contains(professor, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
            if(filteredList.size == 0){
                emptyFilter = true
                getAllSessions {  }
            }
            else{
                emptyFilter = false
                sessions = filteredList
            }
        }

        else if(university.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.university.contains(university, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("university"))
            if(filteredList.size == 0){
                emptyFilter = true
                getAllSessions {  }
            }
            else{
                emptyFilter = false
                sessions = filteredList
            }
        }
        else if(professor.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.tutor.contains(professor, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("tutor"))
            if(filteredList.size == 0){
                emptyFilter = true
                getAllSessions {  }
            }
            else{
                emptyFilter = false
                sessions = filteredList
            }
        }
        else if(course.isNotEmpty()){
            val filteredList = mutableListOf<TutoringSession>()
            for (element in response.body() ?: emptyList()){
                if(element.course.contains(course, ignoreCase = true)){
                    filteredList.add(element)
                }
            }
            RetrofitClient.instance.increaseFilterCount(PostFilterCounterIncreaseRequest("course"))
            if(filteredList.size == 0){
                emptyFilter = true
                getAllSessions {  }
            }
            else{
                emptyFilter = false
                sessions = filteredList
            }
        }

        else{
            getAllSessions {  }
        }

    }*/

    fun getSearchResults(onResult: (Boolean, SearchResultFilterResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSearchResultsFilter()

                if (response.isSuccessful) {
                    onResult(true, response.body())
                } else {
                    onResult(false, null)
                }
            } catch (e: Exception) {
                onResult(false, null)
            }
        }
    }

    fun postTimeToBook(timeToBook: Float, tutorId: Int) {
        viewModelScope.launch {
            try {
                val body = PostTimeToBookRequest(
                    duration = timeToBook,
                    tutorId = tutorId
                )
                val response = RetrofitClient.instance.postTimeToBook(body)
                Log.i("analytics", response.body()?.data ?: "")
            } catch (e: Exception) {
                Log.i("error", e.message ?: "unknown error")
            }
        }
    }

    fun bookingTime(){
        viewModelScope.launch {
            RetrofitClient.instance.bookingTime()
            }
    }

    fun getCachedSession(sessionId: Int): TutoringSession?{
        return tutorCardCache[sessionId]
    }
}