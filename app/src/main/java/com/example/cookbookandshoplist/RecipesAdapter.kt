package com.example.cookbookandshoplist

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipesAdapter(val context: Context, val recipes: List<FoodNameAndPicture>) :
    RecyclerView.Adapter<RecipesAdapter.ViewHolder>() {

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

        var isFavorite = false // Create a boolean to keep track of the favorites status


        holder.apply {
            foodNameTextView.setOnClickListener {
                val intent = Intent(context, FoodInfoActivity::class.java).apply {
                    putExtra("getFoodName", recipes[position].foodName)
                    putExtra("getTime", recipes[position].time)
                    putExtra("getIngredients", recipes[position].ingredients)
                    putExtra("getMethod", recipes[position].method)
                    putExtra("getImage", recipes[position].glideImageUrl)
                }
                context.startActivity(intent)
            }

            // Update the favorite button image based on the favorite status
            updateFavoriteButtonImage(favoriteImageButton, isFavorite)

            favoriteImageButton.setOnClickListener { view ->
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                val firebase = FirebaseFirestore.getInstance()
                if (currentUser != null) {
                    val userId = currentUser.uid
                    val favoriteRecipesRef = firebase.collection("users").document(userId)
                        .collection("favoriteRecipes")
                    val selectedRecipe = recipes[position]

                    // Change the favorite status and update the image of the button
                    isFavorite = !isFavorite
                    updateFavoriteButtonImage(favoriteImageButton, isFavorite)

                    if (isFavorite) {
                        favoriteRecipesRef.add(selectedRecipe)
                            .addOnSuccessListener {
                                Log.d("FAVORITE", "Recipe added to favorites.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FAVORITE", "Error adding recipe to favorites", e)
                            }
                    } else {
                        // Remove the recipe from favorites if it is already a favorite
                        favoriteRecipesRef.whereEqualTo("foodName", selectedRecipe.foodName)
                            .get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    document.reference.delete()
                                        .addOnSuccessListener {
                                            Log.d("FAVORITE", "Recipe removed from favorites.")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("FAVORITE", "Error removing recipe from favorites", e)
                                        }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("FAVORITE", "Error getting documents: ", exception)
                            }
                    }
                } else {
                    // If the user is not logged in, navigate them to the login page
                    val intent = Intent(context, LogInActivity::class.java)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount() = recipes.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var foodNameTextView = itemView.findViewById<TextView>(R.id.foodNameTextView)
        var timeTextView = itemView.findViewById<TextView>(R.id.timeTextView)
        var foodImageView = itemView.findViewById<ImageView>(R.id.foodImageView)
        var favoriteImageButton = itemView.findViewById<ImageButton>(R.id.favoriteImageButton)
    }

     //Function to update the favorite button image based on the favorite status
    private fun updateFavoriteButtonImage(button: ImageButton, isFavorite: Boolean) {
        if (isFavorite) {
            button.setImageResource(R.drawable.fillheart)
        } else {
            button.setImageResource(R.drawable.heart)
        }
    }

}

