package com.dulziz.submission_storyapp.view.user

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dulziz.submission_storyapp.R
import com.dulziz.submission_storyapp.data.Resources
import com.dulziz.submission_storyapp.data.pref.UserPreference
import com.dulziz.submission_storyapp.databinding.ActivityLoginBinding
import com.dulziz.submission_storyapp.view.MainActivity
import com.dulziz.submission_storyapp.view.ViewModelFactory

class LoginActivity : AppCompatActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
    private lateinit var binding : ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupViewModel()
        setupView()
        setupAction()
        setAnimation()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            if (isValid()) {
                val email = binding.etEmail.text.toString()
                val password = binding.etPass.text.toString()
                userViewModel.login(email, password)
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.check_input),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.tvSignup.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setAnimation() {
        val appIcon = ObjectAnimator.ofFloat(binding.icon, View.ALPHA, 1f).setDuration(700)
        val appName = ObjectAnimator.ofFloat(binding.tvStory, View.ALPHA, 1f).setDuration(700)
        val etEmail = ObjectAnimator.ofFloat(binding.etEmail, View.ALPHA, 1f).setDuration(700)
        val etPass = ObjectAnimator.ofFloat(binding.etPass, View.ALPHA, 1f).setDuration(700)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(700)
        val txtHaveAcc = ObjectAnimator.ofFloat(binding.tvDontHaveAcc, View.ALPHA, 1f).setDuration(700)
        val txtSignup = ObjectAnimator.ofFloat(binding.tvSignup, View.ALPHA, 1f).setDuration(700)

        val textAnim = AnimatorSet().apply {
            playTogether(appName, txtSignup, txtHaveAcc)
        }
        val layoutAnim = AnimatorSet().apply {
            playTogether(etPass, etEmail)
        }

        AnimatorSet().apply {
            playSequentially(
                appIcon,
                textAnim,
                layoutAnim,
                btnLogin
            )
            start()
        }
    }

    private fun setupView() {
        userViewModel.userInfo.observe(this) {
            when (it) {
                is Resources.Success -> {
                    showLoad(false)
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
                is Resources.Loading -> showLoad(true)
                is Resources.Error -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    showLoad(false)
                }
            }
        }
    }

    private fun setupViewModel() {
        val pref = UserPreference.getInstance(dataStore)
        userViewModel = ViewModelProvider(this, ViewModelFactory(pref))[UserViewModel::class.java]
    }

    private fun showLoad(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun isValid() =
        binding.etEmail.error == null && binding.etPass.error == null && !binding.etEmail.text.isNullOrEmpty() && !binding.etPass.text.isNullOrEmpty()

}