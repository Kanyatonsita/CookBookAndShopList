package com.example.cookbookandshoplist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class FoodInfoActivity : AppCompatActivity() {

    lateinit var nameTextView : TextView
    lateinit var cookingTimeTextView : TextView
    lateinit var ingredientsTextView : TextView
    lateinit var methodTextView : TextView
    lateinit var imageFoodView : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info)

        nameTextView = findViewById(R.id.nameTextView)
        cookingTimeTextView = findViewById(R.id.cookingTimeTextView)
        ingredientsTextView = findViewById(R.id.ingredientsTextView)
        methodTextView = findViewById(R.id.methodTextView)
        imageFoodView = findViewById(R.id.imageFoodView)



        val intent = intent

        val getFoodName = intent.getStringExtra("getFoodName")
        val getTime = intent.getStringExtra("getTime")
        val getIngredients = intent.getStringExtra("getIngredients")
        val getMethod = intent.getStringExtra("getMethod")
        val getImage = intent.getStringExtra("getImage")


        nameTextView.text = getFoodName
        cookingTimeTextView.text = getTime
        ingredientsTextView.text = getIngredients
        methodTextView.text = getMethod

        Glide.with(this)
            .load(getImage)
            .into(imageFoodView)
    }


    fun backToMainActivity(view: View) {
        finish()
    }
}