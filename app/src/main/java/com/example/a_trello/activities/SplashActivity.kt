package com.example.a_trello.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import com.example.a_trello.databinding.ActivitySplashBinding
import com.example.a_trello.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fullScreen()

        setTitleFont()

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
            var currentUserID = FirestoreClass().getCurrentUserId()
            if (currentUserID.isEmpty()) {
                startActivity(Intent(this, IntroActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }, 2500)
    }

    private fun setTitleFont() {
        val typeFace: Typeface = Typeface.createFromAsset(assets, "carbon bl.otf")
        binding?.tvAppName?.typeface = typeFace
    }

    private fun fullScreen() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}