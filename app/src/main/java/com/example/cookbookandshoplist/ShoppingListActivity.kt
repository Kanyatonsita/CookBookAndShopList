package com.example.cookbookandshoplist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class ShoppingListActivity : AppCompatActivity() {

    lateinit var db : FirebaseFirestore
    lateinit var shopListEditText : EditText
    lateinit var auth : FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: ShoppingListAdapter
    private val shoppingItems = mutableListOf<ShoppingItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        db = Firebase.firestore
        shopListEditText = findViewById(R.id.shopListEditText)

        recyclerView = findViewById(R.id.shoppingListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ShoppingListAdapter(this, shoppingItems)
        recyclerView.adapter = adapter

        val uid = currentUser.uid
        val shoppingListRef = db.collection("users").document(uid).collection("shoppingItems")

        shoppingListRef.addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                shoppingItems.clear()
                for (document in snapshot.documents) {
                    val item = document.toObject(ShoppingItem::class.java)
                    item?.documentId = document.id
                    if (item != null) {
                        shoppingItems.add(item)
                    }
                }
                adapter.notifyDataSetChanged() // Notify adapter after data change
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    fun addItem(view: View) {
        val itemName = shopListEditText.text.toString()
        if (itemName.isNotEmpty()) {
            val existingItem = shoppingItems.find { it.name == itemName }
            if (existingItem != null) {
                // Om varan redan finns i listan, meddela användaren eller ignorerar tillägget
                Toast.makeText(this, "Varan finns redan i listan!", Toast.LENGTH_SHORT).show()
            } else {
                // Om varan inte finns i listan, lägg till den
                val item = ShoppingItem(name = itemName, done = false)
                shopListEditText.setText("")

                val user = auth.currentUser
                if (user != null) {
                    db.collection("users").document(user.uid)
                        .collection("shoppingItems").add(item)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                }
            }
        } else {
            Toast.makeText(this, "Vänligen ange varor!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "ShoppingListActivity"
    }
}


data class ShoppingItem (@DocumentId var documentId : String? = null,
                 var name : String? = null,
                 var done: Boolean = false)