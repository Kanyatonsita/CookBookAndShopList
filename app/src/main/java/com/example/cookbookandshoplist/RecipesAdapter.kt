package com.example.cookbookandshoplist

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class RecipesAdapter (val context: Context, val recipes : List<FoodNameAndPicture>)
    : RecyclerView.Adapter<RecipesAdapter.ViewHolder>() {

        var layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val recipesItemView = layoutInflater.inflate(R.layout.recipes_item, parent, false)
        return ViewHolder(recipesItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val foodRecipes = recipes[position]

        holder.foodNameTextView.text = foodRecipes.foodName
        holder.timeTextView.text = foodRecipes.time

        Glide.with(holder.itemView.context)
            .load(foodRecipes.glideImageUrl)
            .into(holder.foodImageView)

        holder.apply {
            foodNameTextView.setOnClickListener {
                val intent = Intent (context,FoodInfoActivity::class.java)
                intent.putExtra("getFoodName",recipes[position].foodName)
                intent.putExtra("getTime",recipes[position].time)
                intent.putExtra("getIngredients",recipes[position].ingredients)
                intent.putExtra("getMethod",recipes[position].method)
                intent.putExtra("getImage",recipes[position].glideImageUrl)
                context.startActivity(intent)
            }
            Log.d("URL_DEBUG", "Image URL: ${recipes[position].glideImageUrl}")
        }

 //          holder.favoriteImageButton.setOnClickListener{
//            val auth = FirebaseAuth.getInstance()
//            if (auth.currentUser != null) {
//                startActivity(Intent(this, MainActivity::class.java))
//            } else {
//                startActivity(Intent(this, LogInActivity::class.java))
//            }
//        }

    }

    override fun getItemCount() = recipes.size

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        var foodNameTextView = itemView.findViewById<TextView>(R.id.foodNameTextView)
        var timeTextView = itemView.findViewById<TextView>(R.id.timeTextView)
        var foodImageView = itemView.findViewById<ImageView>(R.id.foodImageView)
        var favoriteImageButton = itemView.findViewById<ImageButton>(R.id.favoriteImageButton)

    }
}