package com.dulziz.submission_storyapp.view.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dulziz.submission_storyapp.data.Resources
import com.dulziz.submission_storyapp.data.network.api.ApiConfig
import com.dulziz.submission_storyapp.data.network.response.RegisterResponse
import com.dulziz.submission_storyapp.data.pref.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Reader

class AddStoryViewModel(private val pref: UserPreference) : ViewModel() {

    private val _storyUploadLiveData = MutableLiveData<Resources<String>>()
    val uploadInfo: LiveData<Resources<String>> = _storyUploadLiveData

    suspend fun uploadStory(
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
    ) {
        _storyUploadLiveData.postValue(Resources.Loading())

        val client = ApiConfig.createApiClient().addStory(
            token = "Bearer ${pref.getToken().first()}",
            imageMultipart,
            description
        )

        handleUploadResponse(client)
    }

    private fun handleUploadResponse(client: Call<RegisterResponse>) {
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.isSuccessful) {
                    handleUploadSuccess(response.body()?.message)
                } else {
                    val errorStream = response.errorBody()?.charStream()
                    handleUploadError(errorStream)
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                handleUploadFailure(t.message)
            }
        })
    }

    private fun handleUploadSuccess(message: String?) {
        _storyUploadLiveData.postValue(Resources.Success(message))
    }

    private fun handleUploadError(errorStream: Reader?) {
        val errorResponse = Gson().fromJson(errorStream, RegisterResponse::class.java)
        _storyUploadLiveData.postValue(Resources.Error(errorResponse.message))
    }

    private fun handleUploadFailure(message: String?) {
        _storyUploadLiveData.postValue(Resources.Error(message))
    }
}