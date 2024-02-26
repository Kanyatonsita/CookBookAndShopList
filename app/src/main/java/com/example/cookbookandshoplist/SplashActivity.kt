package com.example.cookbookandshoplist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION = 2000L
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextView()
        }, SPLASH_DURATION)
    }

    private fun navigateToNextView(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}