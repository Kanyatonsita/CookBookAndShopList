package com.example.cookbookandshoplist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.bumptech.glide.Glide

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

        val imageRef = Firebase.storage.reference.child("images/${foodRecipes.glideImageUrl}")
        imageRef.downloadUrl.addOnSuccessListener { Uri -> val imageUrl = Uri.toString()
            Glide.with(context)
                .load(imageUrl)
                .into(holder.foodImageView)}
    }

    override fun getItemCount() = recipes.size

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        var foodNameTextView = itemView.findViewById<TextView>(R.id.foodNameTextView)
        var timeTextView = itemView.findViewById<TextView>(R.id.timeTextView)
        var foodImageView = itemView.findViewById<ImageView>(R.id.foodImageView)
        var favoriteImageButton = itemView.findViewById<ImageButton>(R.id.favoriteImageButton)

    }
}