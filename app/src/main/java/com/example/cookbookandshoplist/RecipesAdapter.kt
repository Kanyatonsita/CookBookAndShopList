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

            // Check if the user is logged in
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Hämta favoritstatus baserat på användar-ID
                val userId = getCurrentUserId()

                if (userId != null) {
                    // Hämta favoritstatus för användaren från databasen
                    val firebase = FirebaseFirestore.getInstance()
                    val favoriteRecipesRef = firebase.collection("users").document(userId)
                        .collection("favoriteRecipes").whereEqualTo("foodName", foodRecipes.foodName)

                    favoriteRecipesRef.get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                // Receptet finns i användarens favoriter i databasen, uppdatera favoritknappens bild
                                updateFavoriteButtonImage(holder.favoriteImageButton, true)
                            } else {
                                // Receptet finns inte i användarens favoriter i databasen, uppdatera favoritknappens bild
                                updateFavoriteButtonImage(holder.favoriteImageButton, false)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FAVORITE", "Error getting documents: ", exception)
                        }
                }

                // Update the favorite button image based on the favorite status
                val isFavorite = foodRecipes.foodName?.let { getFavoriteStatusForUser(it, userId) }

                favoriteImageButton.setOnClickListener { view ->
                    val firebase = FirebaseFirestore.getInstance()
                    val userId = currentUser.uid
                    val favoriteRecipesRef = firebase.collection("users").document(userId)
                        .collection("favoriteRecipes")
                    val selectedRecipe = recipes[position]

                    // Change the favorite status and update the image of the button
                    val newFavoriteStatus = !isFavorite!!
                    updateFavoriteButtonImage(favoriteImageButton, newFavoriteStatus)

                    if (newFavoriteStatus) {
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

                    // Save the updated favorite status to SharedPreferences
                    foodRecipes.foodName?.let { saveFavoriteStatusForUser(it, newFavoriteStatus, userId) }
                }
            } else {
                // User is not logged in, display default favorite button image
                favoriteImageButton.setImageResource(R.drawable.heart)
                // Redirect user to login page if they try to interact with the favorite button
                favoriteImageButton.setOnClickListener {
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

    private fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun getFavoriteStatusForUser(foodName: String, userId: String?): Boolean {
        if (userId.isNullOrEmpty()) {
            return false // Om användar-ID är tomt, returnera false (standardvärde)
        }

        val sharedPreferences = context.getSharedPreferences("Favorites_$userId", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(foodName, false)
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

