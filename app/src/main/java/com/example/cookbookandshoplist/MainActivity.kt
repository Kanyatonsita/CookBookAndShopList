package com.example.cookbookandshoplist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goToLogInActivity(view: View) {
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
    }
}