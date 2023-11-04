package com.dulziz.submission_storyapp.data.network.response

import com.dulziz.submission_storyapp.data.model.Story
import com.google.gson.annotations.SerializedName

data class GetAllStoryResponse(
    @SerializedName("error")
    val error: Boolean?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("listStory")
    val listStory: List<Story>
)