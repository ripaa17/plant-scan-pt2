package com.example.plantsscanning.ui.analisis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.plantsscanning.R
import com.example.plantsscanning.api.ApiConfig
import com.example.plantsscanning.model.TomatoResponse
import com.example.plantsscanning.databinding.FragmentAnalisisBinding
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

class AnalisisFragment : Fragment() {
    private var _binding: FragmentAnalisisBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalisisBinding.inflate(inflater, container, false)

        val imageBase64 = arguments?.getString("IMAGE_BASE64")

        val diseaseName = arguments?.getString("DISEASE_NAME") ?: "Unknown Disease"
        val description = arguments?.getString("DESCRIPTION") ?: "No Description"
        val action = arguments?.getString("ACTION") ?: "No Action"


        val imageView = binding.plantImage

        val drawable = imageView.drawable

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.baseline_image_24)
        binding.plantImage.setImageBitmap(bitmap)


        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val resizedBitmap = resizeBitmap(bitmap, 800, 800)
        } else {
            Toast.makeText(context, "Gambar tidak valid atau tidak ada", Toast.LENGTH_SHORT).show()
        }

        binding.tvDiseaseName.text = diseaseName
        binding.tvDiseaseDetail.text = description
        binding.tvSolutionDetail.text = action

        Log.d("AnalisisFragment", "Received Base64: ${imageBase64?.length}")

        if (imageBase64 != null) {
            try {
                val imageBitmap = convertBase64ToBitmap(imageBase64)
                analyzeImage(imageBitmap)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, "Gambar base64 tidak valid", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Gambar tidak tersedia", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun convertBase64ToBitmap(base64: String): Bitmap {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeStream(ByteArrayInputStream(decodedBytes))
    }

    private fun analyzeImage(bitmap: Bitmap) {
        val file = convertBitmapToFile(bitmap)
        val requestFile = RequestBody.create(
            "image/jpeg".toMediaType(),
            file
        )
        val body = MultipartBody.Part.createFormData("imagefile", file.name, requestFile)

        val apiService = ApiConfig.apiService
        val call = apiService.uploadImage(body)

        call.enqueue(object : Callback<TomatoResponse> {
            override fun onResponse(call: Call<TomatoResponse>, response: Response<TomatoResponse>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    responseData?.let {
                        binding.tvDiseaseName.text = it.status ?: "Tidak ada data nama penyakit"
                        binding.tvDiseaseDetail.text = it.message ?: "Tidak ada detail penyebab"
                    } ?: run {
                        Toast.makeText(context, "Response body kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal memanggil API", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TomatoResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        file.delete()
    }

    private fun convertBitmapToFile(bitmap: Bitmap): File {
        val file = File(context?.cacheDir, "image.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val bitmapRatio: Float = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (bitmapRatio > 1) {
            newWidth = maxWidth
            newHeight = (maxWidth / bitmapRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * bitmapRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}


