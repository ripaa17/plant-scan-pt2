package com.example.plantsscanning.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.plantsscanning.R
import com.example.plantsscanning.databinding.ActivityMainBinding
import com.example.plantsscanning.ui.upload.UploadActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var progressBarContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()

        progressBarContainer = binding.progressBarContainer

        binding.btntomato.setOnClickListener {
            showProgressBarAndNavigate("Tomato", progressBarContainer)
        }

        binding.btnchili.setOnClickListener {
            showProgressBarAndNavigate("Chili", progressBarContainer)
        }

        binding.btngrape.setOnClickListener {
            showProgressBarAndNavigate("Grape", progressBarContainer)
        }
    }

    private fun playAnimation() {
        println("Animation started")
        val textView = ObjectAnimator.ofFloat(binding.textViewPlant, View.ALPHA, 1f).setDuration(900)
        val btntomato = ObjectAnimator.ofFloat(binding.btntomato, View.ALPHA, 1f).setDuration(900)
        val btnchili = ObjectAnimator.ofFloat(binding.btnchili, View.ALPHA, 1f).setDuration(900)
        val btngrape = ObjectAnimator.ofFloat(binding.btngrape, View.ALPHA, 1f).setDuration(900)

        AnimatorSet().apply {
            playSequentially(textView, btntomato, btnchili, btngrape)
            start()
        }
    }

    private fun showProgressBarAndNavigate(plantType: String, progressBarContainer: FrameLayout) {
        progressBarContainer.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            progressBarContainer.visibility = View.GONE
            val intent = Intent(this, UploadActivity::class.java)
            intent.putExtra("PLANT_TYPE", plantType)
            startActivity(intent)
        }, 3000)
    }
}

