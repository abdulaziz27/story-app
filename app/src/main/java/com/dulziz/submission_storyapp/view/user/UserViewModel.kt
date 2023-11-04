package com.dulziz.submission_storyapp.view.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dulziz.submission_storyapp.data.Resources
import com.dulziz.submission_storyapp.data.network.api.ApiConfig
import com.dulziz.submission_storyapp.data.network.request.LoginRequest
import com.dulziz.submission_storyapp.data.network.request.RegisterRequest
import com.dulziz.submission_storyapp.data.network.response.LoginResponse
import com.dulziz.submission_storyapp.data.network.response.RegisterResponse
import com.dulziz.submission_storyapp.data.pref.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel(private val pref: UserPreference) : ViewModel() {
    private val _userInfo = MutableLiveData<Resources<String>>()
    val userInfo: LiveData<Resources<String>> = _userInfo

    fun login(email: String, password: String) {
        _userInfo.postValue(Resources.Loading())
        val client = ApiConfig.createApiClient().login(LoginRequest(email, password))

        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.loginResult?.token

                    result?.let { saveUserToken(it) }
                    _userInfo.postValue(Resources.Success(result))
                } else {
                    val errorResponse = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        RegisterResponse::class.java
                    )
                    _userInfo.postValue(Resources.Error(errorResponse.message))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(
                    UserViewModel::class.java.simpleName,
                    "onFailure"
                )
                _userInfo.postValue(Resources.Error(t.message))
            }
        })
    }


    fun register(name: String, email: String, password: String) {
        _userInfo.postValue(Resources.Loading())
        val client = ApiConfig.createApiClient().register(RegisterRequest(name, email, password))

        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message.toString()
                    _userInfo.postValue(Resources.Success(message))
                } else {
                    val errorResponse = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        RegisterResponse::class.java
                    )
                    _userInfo.postValue(Resources.Error(errorResponse.message))
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e(
                    UserViewModel::class.java.simpleName,
                    "onFailure"
                )
                _userInfo.postValue(Resources.Error(t.message))
            }
        })
    }

    fun logout() = deleteUserToken()

    fun getUserToken() = pref.getToken().asLiveData()

    private fun saveUserToken(key: String) {
        viewModelScope.launch {
            pref.saveToken(key)
        }
    }

    private fun deleteUserToken() {
        viewModelScope.launch {
            pref.deleteToken()
        }
    }
}