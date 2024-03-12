package com.example.cookbookandshoplist

import android.content.ContentValues.TAG
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

class ShoppingListAdapter (val context: Context, private val shoppingList: MutableList<ShoppingItem>) :
    RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>(){

    var layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val shoppingItemView = layoutInflater.inflate(R.layout.shopping_item, parent, false)
        return ViewHolder(shoppingItemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shoppingItems = shoppingList[position]

        holder.shoppingItemTextView.text = shoppingItems.name
        holder.shoppingListCheckBox.isChecked = shoppingItems.done // Sätt CheckBox-tillståndet baserat på datan i ShoppingItem

        holder.shoppingListCheckBox.setOnCheckedChangeListener(null) // Ta bort eventuell tidigare lyssnare

        holder.shoppingListCheckBox.setOnCheckedChangeListener { _, isChecked ->
            shoppingItems.done = isChecked // Uppdatera ShoppingItem-done-attributet
            updateItemInFirestore(shoppingItems)
        }

    }
    override fun getItemCount() = shoppingList.size
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var shoppingItemTextView= itemView.findViewById<TextView>(R.id.shoppingItemTextView)
        var imageButtonDelete = itemView.findViewById<ImageButton>(R.id.imageButtonDelete)
        var shoppingListCheckBox = itemView.findViewById<CheckBox>(R.id.shoppingListCheckBox)

        init {
            imageButtonDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val itemToRemove = shoppingList[position]
                    deleteItem(itemToRemove)
                }
            }
        }
    }

    private fun deleteItem(item: ShoppingItem) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val itemRef = db.collection("users").document(userId).collection("shoppingItems").document(item.documentId!!)
            itemRef.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Item deleted successfully")
                    shoppingList.remove(item)
                    notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error deleting item", e)
                }
        }
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