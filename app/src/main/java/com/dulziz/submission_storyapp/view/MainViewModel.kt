package com.dulziz.submission_storyapp.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dulziz.submission_storyapp.data.Resources
import com.dulziz.submission_storyapp.data.model.Story
import com.dulziz.submission_storyapp.data.network.api.ApiConfig
import com.dulziz.submission_storyapp.data.network.response.GetAllStoryResponse
import com.dulziz.submission_storyapp.data.network.response.RegisterResponse
import com.dulziz.submission_storyapp.data.pref.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreference) : ViewModel(){
    private val _stories = MutableLiveData<Resources<ArrayList<Story>>>()
    val stories: LiveData<Resources<ArrayList<Story>>> = _stories

    suspend fun getStories() {
        _stories.postValue(Resources.Loading())
        val client =
            ApiConfig.createApiClient().getStories(token = "Bearer ${pref.getToken().first()}")

        client.enqueue(object : Callback<GetAllStoryResponse> {
            override fun onResponse(call: Call<GetAllStoryResponse>, response: Response<GetAllStoryResponse>) {

                if (response.isSuccessful) {
                    response.body()?.let {
                        val listStory = it.listStory
                        _stories.postValue(Resources.Success(ArrayList(listStory)))
                    }
                } else {
                    val errorResponse = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        RegisterResponse::class.java
                    )
                    _stories.postValue(Resources.Error(errorResponse.message))
                }
            }

            override fun onFailure(call: Call<GetAllStoryResponse>, t: Throwable) {
                Log.e(
                    MainViewModel::class.java.simpleName,
                    "onFailure"
                )
                _stories.postValue(Resources.Error(t.message))
            }
        })
    }
}