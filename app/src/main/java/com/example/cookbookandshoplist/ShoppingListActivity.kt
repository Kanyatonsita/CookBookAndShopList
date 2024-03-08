package com.example.cookbookandshoplist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class ShoppingListActivity : AppCompatActivity() {

    lateinit var db : FirebaseFirestore
    lateinit var shopListEditText : EditText
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        auth = Firebase.auth
        db = Firebase.firestore
        shopListEditText = findViewById(R.id.shopListEditText)


    }

    fun addItem(view: View) {
        val item = ShoppingItem(name = shopListEditText.text.toString())
        shopListEditText.setText("")

        val user = auth.currentUser
        if (user == null){
            return
        }

        db.collection("users").document(user.uid)
            .collection("shoppingItems").add(item)
    }
}

data class ShoppingItem (@DocumentId var documentId : String? = null,
                 var name : String? = null,
                 var done: Boolean = false)