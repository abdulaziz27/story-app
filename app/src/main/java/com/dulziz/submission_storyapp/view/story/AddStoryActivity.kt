package com.dulziz.submission_storyapp.view.story

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dulziz.submission_storyapp.R
import com.dulziz.submission_storyapp.data.Resources
import com.dulziz.submission_storyapp.data.pref.UserPreference
import com.dulziz.submission_storyapp.databinding.ActivityAddStoryBinding
import com.dulziz.submission_storyapp.utils.compressCustomImage
import com.dulziz.submission_storyapp.utils.convertUriToFile
import com.dulziz.submission_storyapp.utils.createCustomTempFile
import com.dulziz.submission_storyapp.view.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var currentPhotoPath: String
    private lateinit var addStoryViewModel: AddStoryViewModel

    private var selectedFile: File? = null

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!areAllPermissionsGranted()) {
                showToast(getString(R.string.permission_not_granted))
                finish()
            }
        }
    }

    private fun areAllPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        if (!areAllPermissionsGranted()) {
            requestPermissions()
        }
        initializeViewModel()

        binding.btnCamera.setOnClickListener { startCamera() }
        binding.btnGallery.setOnClickListener { openGallery() }
        binding.btnUpload.setOnClickListener { uploadImage() }
    }

    private fun initializeViewModel() {
        val userPreferences = UserPreference.getInstance(dataStore)
        addStoryViewModel = ViewModelProvider(this, ViewModelFactory(userPreferences))[AddStoryViewModel::class.java]

        addStoryViewModel.uploadInfo.observe(this) {
            when (it) {
                is Resources.Success -> {
                    it.data?.let { data -> showToast(data) }
                    finish()
                    showProgressBar(false)
                }
                is Resources.Loading -> showProgressBar(true)
                is Resources.Error -> {
                    it.message?.let { message -> showToast(message) }
                    showProgressBar(false)
                }
            }
        }
    }

    private fun uploadImage() {
        if (selectedFile != null) {
            val compressedFile = compressCustomImage(selectedFile as File)

            val description = "${binding.etDesc.text}".toRequestBody("text/plain".toMediaType())
            val requestImageFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                compressedFile.name,
                requestImageFile
            )

            CoroutineScope(Dispatchers.IO).launch {
                addStoryViewModel.uploadStory(imageMultipart, description)
            }
        } else {
            showToast(getString(R.string.please_select_an_image))
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        galleryLauncher.launch(chooser)
    }

    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "com.dulziz.submission_storyapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraLauncher.launch(intent)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data as? Uri
            if (selectedImageUri != null) {
                val selectedFile = convertUriToFile(selectedImageUri, this@AddStoryActivity)
                this.selectedFile = selectedFile
                binding.previewImage.setImageURI(selectedImageUri)
            } else {
                showToast(getString(R.string.failed_retrieve))
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val selectedFile = File(currentPhotoPath)
            this.selectedFile = selectedFile

            val resultBitmap = BitmapFactory.decodeFile(selectedFile?.path)
            binding.previewImage.setImageBitmap(resultBitmap)
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
        binding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}