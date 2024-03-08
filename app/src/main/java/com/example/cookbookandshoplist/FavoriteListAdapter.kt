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

class FavoriteListAdapter (val context: Context, private val favoriteList: List<FoodNameAndPicture>) :
    RecyclerView.Adapter<FavoriteListAdapter.ViewHolder>() {

    var layoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val favoriteItemView = layoutInflater.inflate(R.layout.favorite_item, parent, false)
        return ViewHolder(favoriteItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favoriteRecipes = favoriteList[position]

        holder.textViewFoodName.text = favoriteRecipes.foodName
        holder.textViewTime.text = favoriteRecipes.time

        Glide.with(holder.itemView.context)
            .load(favoriteRecipes.glideImageUrl)
            .into(holder.favoriteImageView)


        holder.apply {
            textViewFoodName.setOnClickListener {
                val intent = Intent(context, FoodInfoActivity::class.java).apply {
                    putExtra("getFoodName", favoriteList[position].foodName)
                    putExtra("getTime", favoriteList[position].time)
                    putExtra("getIngredients", favoriteList[position].ingredients)
                    putExtra("getMethod", favoriteList[position].method)
                    putExtra("getImage", favoriteList[position].glideImageUrl)
                }
                context.startActivity(intent)
            }

            deleteImageButton.setOnClickListener{
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                val firebase = FirebaseFirestore.getInstance()
                if (currentUser != null) {
                    val userId = currentUser.uid
                    val favoriteListRef = firebase.collection("users").document(userId)
                        .collection("favoriteRecipes")
                    val selectedRecipe = favoriteList[position]

                    favoriteListRef.whereEqualTo("foodName", selectedRecipe.foodName)
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
            }
        }

        }

    override fun getItemCount() = favoriteList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewFoodName= itemView.findViewById<TextView>(R.id.textViewFoodName)
        var textViewTime = itemView.findViewById<TextView>(R.id.textViewTime)
        var favoriteImageView = itemView.findViewById<ImageView>(R.id.favoriteImageView)
        var deleteImageButton = itemView.findViewById<ImageButton>(R.id.deleteImageButton)
    }

}