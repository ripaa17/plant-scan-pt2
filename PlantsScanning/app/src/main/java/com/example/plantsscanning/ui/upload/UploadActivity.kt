package com.example.plantsscanning.ui.upload

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.plantsscanning.R
import com.example.plantsscanning.api.ApiConfig
import com.example.plantsscanning.databinding.ActivityMainBinding
import com.example.plantsscanning.databinding.ActivityUploadBinding
import com.example.plantsscanning.model.TomatoResponse
import com.example.plantsscanning.ui.MainActivity
import com.example.plantsscanning.ui.analisis.AnalisisFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest
import android.util.Base64
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var progressBarContainer: FrameLayout


    private lateinit var imageView: ImageView
    private lateinit var btnCamera: Button
    private lateinit var btnGallery: Button
    private lateinit var btnBack: Button
    private lateinit var btnAnalyze: Button
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = findViewById(R.id.linearLayoutCamera)
        btnCamera = findViewById(R.id.btncamera)
        btnGallery = findViewById(R.id.btngalery)
        btnBack = findViewById(R.id.btnBack)
        btnAnalyze = findViewById(R.id.btnAnalyze)

        playAnimation()

        progressBarContainer = binding.progressBarContainer


        val plantType = intent.getStringExtra("PLANT_TYPE")
        if (plantType != null) {
            val plantTypeTextView: TextView = findViewById(R.id.plantTypeTextView)
            plantTypeTextView.text = "Plant Type: $plantType"
        } else {
            Toast.makeText(this, "Plant type data is missing", Toast.LENGTH_SHORT).show()
        }



        btnCamera.setOnClickListener {
            checkAndRequestCameraPermission()
        }


        btnGallery.setOnClickListener {
            openGallery()
        }

        btnBack.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
        }

        btnAnalyze.setOnClickListener {
            progressBarContainer.visibility = View.VISIBLE

            if (imageBitmap == null) {
                showSnackbar("Gambar tidak valid atau belum dipilih.")
                progressBarContainer.visibility = View.GONE
                return@setOnClickListener
            }


            val file = File(cacheDir, "image.jpg")
            val outputStream = FileOutputStream(file)
            imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()


            val requestFile = RequestBody.create(
                "image/jpeg".toMediaType(),
                file
            )
            val body = MultipartBody.Part.createFormData("imagefile", file.name, requestFile)


            val apiService = ApiConfig.apiService
            apiService.uploadImage(body).enqueue(object : Callback<TomatoResponse> {
                override fun onResponse(call: Call<TomatoResponse>, response: Response<TomatoResponse>) {
                    progressBarContainer.visibility = View.GONE
                    if (response.isSuccessful) {
                        val tomatoData = response.body()
                        val message = tomatoData?.label ?: "Data tidak tersedia."
                        showSnackbar("Data berhasil dimuat: $message")

                        val imageBase64 = convertBitmapToBase64(imageBitmap!!)
                        Log.d("UploadActivity", "Base64 String Length: ${imageBase64.length}")

                        val bundle = Bundle().apply {
                            putString("DISEASE_NAME", tomatoData?.label)
                            putString("DESCRIPTION", tomatoData?.description)
                            putString("ACTION", tomatoData?.action)
                            putString("IMAGE_BASE64", imageBase64)
                        }

                        Log.d("UploadActivity", "DISEASE_NAME: Nama Penyakit, IMAGE_BASE64: ${imageBase64.length}")


                        val fragment = AnalisisFragment().apply {
                            arguments = bundle
                        }

                        Log.d("FragmentTransaction", "Navigating to AnalisisFragment with data: $bundle")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                        Log.d("FragmentTransaction", "Transaction committed.")

                    } else {
                        val errorMsg = response.message() ?: "Unknown error"
                        showSnackbar("Gagal memuat data: $errorMsg")
                    }
                }

                override fun onFailure(call: Call<TomatoResponse>, t: Throwable) {
                    progressBarContainer.visibility = View.GONE
                    showSnackbar("Kesalahan jaringan: ${t.message}")
                }
            })
        }





    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun logError(message: String) {
        android.util.Log.e("UploadActivity", message)
    }



    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun playAnimation() {
        println("Animation started")
        val plantTypeTextView = ObjectAnimator.ofFloat(binding.plantTypeTextView, View.ALPHA, 1f).setDuration(900)
        val layoutCamera = ObjectAnimator.ofFloat(binding.linearLayoutCamera, View.ALPHA, 1f).setDuration(900)
        val btncamera = ObjectAnimator.ofFloat(binding.btncamera, View.ALPHA, 1f).setDuration(900)
        val btngalery = ObjectAnimator.ofFloat(binding.btngalery, View.ALPHA, 1f).setDuration(900)
        val btnback = ObjectAnimator.ofFloat(binding.btnBack, View.ALPHA, 1f).setDuration(900)
        val btnanalyze = ObjectAnimator.ofFloat(binding.btnAnalyze, View.ALPHA, 1f).setDuration(900)

        AnimatorSet().apply {
            playSequentially(plantTypeTextView, layoutCamera, btncamera, btngalery, btnback, btnanalyze)
            start()
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }


    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }



    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {

                    imageBitmap = data?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(imageBitmap)
                }
                GALLERY_REQUEST_CODE -> {

                    val imageUri = data?.data
                    imageUri?.let {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        imageView.setImageBitmap(bitmap)
                        imageBitmap = bitmap
                    }
                }
            }
        } else {
            Toast.makeText(this, "Gagal memilih gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
