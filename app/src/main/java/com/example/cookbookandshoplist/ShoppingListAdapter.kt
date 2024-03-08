package com.example.cookbookandshoplist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShoppingListAdapter (val context: Context, private val shoppingList: List<ShoppingItem>) :
    RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>(){

    var layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val shoppingItemView = layoutInflater.inflate(R.layout.shopping_item, parent, false)
        return ViewHolder(shoppingItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shoppingItems = shoppingList[position]

        holder.shoppingItemTextView.text = shoppingItems.name
        holder.shoppingListCheckBox.isChecked = shoppingItems.done

        holder.shoppingListCheckBox.setOnCheckedChangeListener { _, isChecked ->
            shoppingItems.done = isChecked
            updateItemInFirestore(shoppingItems)
        }

        holder.imageButtonDelete.setOnClickListener{
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            val firebase = FirebaseFirestore.getInstance()
            if (currentUser != null) {
                val userId = currentUser.uid
                val shoppingListRef = firebase.collection("users").document(userId)
                    .collection("shoppingItems")
                val selectedRecipe = shoppingList[position]

                shoppingListRef.whereEqualTo("name", selectedRecipe.name)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    Log.d("ITEM", "Recipe removed from shoppingList.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ITEM", "Error removing recipe from shoppingLIst", e)
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ITEM", "Error getting documents: ", exception)
                    }
            }
        }
    }
    override fun getItemCount() = shoppingList.size
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var shoppingItemTextView= itemView.findViewById<TextView>(R.id.shoppingItemTextView)
        var imageButtonDelete = itemView.findViewById<ImageButton>(R.id.imageButtonDelete)
        var shoppingListCheckBox = itemView.findViewById<CheckBox>(R.id.shoppingListCheckBox)
    }

    private fun updateItemInFirestore(shoppingItem: ShoppingItem) {
        val auth = FirebaseAuth.getInstance()
        val userCurrent = auth.currentUser
        val firebase = FirebaseFirestore.getInstance()

        if (userCurrent != null) {
            val userId = userCurrent.uid
            val shoppingListRef = firebase.collection("users").document(userId)
                .collection("shoppingItems")

            shoppingListRef.document(shoppingItem.documentId!!)
                .set(shoppingItem)
                .addOnSuccessListener {
                    Log.d("ITEM", "Shopping item updated successfully in Firestore.")
                }
                .addOnFailureListener { e ->
                    Log.e("ITEM", "Error updating shopping item in Firestore", e)
                }
        }
    }
}