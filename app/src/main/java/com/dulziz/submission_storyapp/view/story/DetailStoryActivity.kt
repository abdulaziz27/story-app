package com.dulziz.submission_storyapp.view.story

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dulziz.submission_storyapp.data.model.Story
import com.dulziz.submission_storyapp.databinding.ActivityDetailStoryBinding

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater )
        setContentView(binding.root)
        supportActionBar?.hide()

        setupView()
    }

    private fun setupView() {
        val detail = intent.getParcelableExtra<Story>(EXTRA_DETAIL)
        displayStoryDetails(detail)
    }

    private fun displayStoryDetails(detail: Story?) {
        binding.apply {
            tvNameDetail.text = detail?.name
            tvDesc.text = detail?.description
        }

        detail?.photoUrl?.let {
            binding.imgStoryDetail.loadImage(it)
        }
    }

    fun ImageView.loadImage(url: String?) {
        Glide.with(this)
            .load(url)
            .into(this)
    }

    companion object {
        const val EXTRA_DETAIL = "extra_detail"
    }
}