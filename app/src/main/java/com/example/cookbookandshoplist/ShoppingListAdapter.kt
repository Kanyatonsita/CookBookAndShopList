package com.example.cookbookandshoplist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
    }
    override fun getItemCount() = shoppingList.size
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var shoppingItemTextView= itemView.findViewById<TextView>(R.id.shoppingItemTextView)
        var imageButtonDelete = itemView.findViewById<ImageButton>(R.id.imageButtonDelete)
    }
}