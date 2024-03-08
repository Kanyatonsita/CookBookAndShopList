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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipesAdapter(val context: Context, val recipes: List<FoodNameAndPicture>) :
    RecyclerView.Adapter<RecipesAdapter.ViewHolder>() {

    var layoutInflater = LayoutInflater.from(context)

    lateinit var auth: FirebaseAuth


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val recipesItemView = layoutInflater.inflate(R.layout.recipes_item, parent, false)
        return ViewHolder(recipesItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val foodRecipes = recipes[position]
        auth = FirebaseAuth.getInstance()

        holder.foodNameTextView.text = foodRecipes.foodName
        holder.timeTextView.text = foodRecipes.time

        Glide.with(holder.itemView.context)
            .load(foodRecipes.glideImageUrl)
            .into(holder.foodImageView)

        val userId = getCurrentUserId()

        if (userId != null) {
            val favoriteRecipesRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                .collection("favoriteRecipes").whereEqualTo("foodName", foodRecipes.foodName)

            favoriteRecipesRef.get()
                .addOnSuccessListener { documents ->
                    val isFavorite = !documents.isEmpty
                    updateFavoriteButtonImage(holder.favoriteImageButton, isFavorite)

                    holder.favoriteImageButton.setOnClickListener {
                        toggleFavoriteStatus(holder, foodRecipes, userId)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FAVORITE", "Error getting documents: ", exception)
                }
        } else {
            // User is not logged in, display default favorite button image
            holder.favoriteImageButton.setImageResource(R.drawable.heart)
            // Redirect user to login page if they try to interact with the favorite button
            holder.favoriteImageButton.setOnClickListener {
                val intent = Intent(context, LogInActivity::class.java)
                context.startActivity(intent)
            }
        }

        holder.foodNameTextView.setOnClickListener {
            val intent = Intent(context, FoodInfoActivity::class.java).apply {
                putExtra("getFoodName", foodRecipes.foodName)
                putExtra("getTime", foodRecipes.time)
                putExtra("getIngredients", foodRecipes.ingredients)
                putExtra("getMethod", foodRecipes.method)
                putExtra("getImage", foodRecipes.glideImageUrl)
            }
            context.startActivity(intent)
        }
    }
    private fun toggleFavoriteStatus(holder: ViewHolder, recipe: FoodNameAndPicture, userId: String) {
        val firebase = FirebaseFirestore.getInstance()
        val favoriteRecipesRef = firebase.collection("users").document(userId)
            .collection("favoriteRecipes")

        favoriteRecipesRef.whereEqualTo("foodName", recipe.foodName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Recipe is not in favorites, add it
                    favoriteRecipesRef.add(recipe)
                        .addOnSuccessListener {
                            Log.d("FAVORITE", "Recipe added to favorites.")
                            updateFavoriteButtonImage(holder.favoriteImageButton, true)
                            // Save the updated favorite status to SharedPreferences
                            recipe.foodName?.let { saveFavoriteStatusForUser(it, true, userId) }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FAVORITE", "Error adding recipe to favorites", e)
                        }
                } else {
                    // Recipe is in favorites, remove it
                    for (document in documents) {
                        document.reference.delete()
                            .addOnSuccessListener {
                                Log.d("FAVORITE", "Recipe removed from favorites.")
                                updateFavoriteButtonImage(holder.favoriteImageButton, false)
                                // Save the updated favorite status to SharedPreferences
                                recipe.foodName?.let { saveFavoriteStatusForUser(it, false, userId) }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FAVORITE", "Error removing recipe from favorites", e)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FAVORITE", "Error getting documents: ", exception)
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

    private fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun saveFavoriteStatusForUser(foodName: String, isFavorite: Boolean, userId: String?) {
        if (userId.isNullOrEmpty()) {
            return // Om användar-ID är tomt, gör inget
        }

        val sharedPreferences = context.getSharedPreferences("Favorites_$userId", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(foodName, isFavorite)
        editor.apply()
    }

}

