package com.tutorapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorapp.models.SearchResultResponse
import com.tutorapp.remote.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Base64
import com.tutorapp.models.SuggestedPriceResponse
import org.json.JSONObject


class AddCourseViewModel : ViewModel() {
    fun getSearchResults(onResult: (Boolean, SearchResultResponse) -> SearchResultResponse): SearchResultResponse {

        val emptyData: SearchResultResponse = mapOf(
            "" to mapOf(
                "" to listOf(0)
            )
        ) as SearchResultResponse
        var resposeData = emptyData

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSearchResults()
                if (response.isSuccessful) {
                    resposeData = response.body()?.data as SearchResultResponse

                    onResult(true, response.body()?.data as SearchResultResponse)

                } else {
                    onResult(false, emptyData)
                }
            } catch (e: Exception) {
                onResult(false, emptyData)
            }
        }

        return resposeData;
    }

    fun getSuggestedPrice(tutorID: Int, universityName: String, onResult: (Boolean, SuggestedPriceResponse) -> SuggestedPriceResponse): SuggestedPriceResponse {
        var resposeData = 0 as SuggestedPriceResponse

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getSearchResults()
                if (response.isSuccessful) {
                    resposeData = response.body()?.data as SuggestedPriceResponse

                    onResult(true, response.body()?.data as SuggestedPriceResponse)

                } else {
                    onResult(false, 0 as SuggestedPriceResponse)
                }
            } catch (e: Exception) {
                onResult(false, 0  as SuggestedPriceResponse)
            }
        }

        return resposeData;
    }
}
