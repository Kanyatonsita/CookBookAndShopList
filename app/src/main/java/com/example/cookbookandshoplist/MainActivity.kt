package com.example.cookbookandshoplist

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.StorageReference
import java.io.InputStream

@GlideModule
class AppGlide : AppGlideModule(){
    override fun registerComponents(
        context: Context,
        glide: Glide,
        registry: Registry
    ) {
        super.registerComponents(context, glide, registry)
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )

    }
}
class MainActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Check the user's login status
        updateLogInTextStatus()

        val db = Firebase.firestore
        val docRef = db.collection("Recipes")

        recyclerView = findViewById(R.id.recipesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = RecipesAdapter(this, DataManagerFoodName.foodName)
        recyclerView.adapter = adapter

        docRef.addSnapshotListener{ snapshop, e ->
            if(snapshop != null){
                DataManagerFoodName.foodName.clear()

                for( document in snapshop.documents) {
                    val item = document.toObject<FoodNameAndPicture>()
                    if(item != null) {

                        DataManagerFoodName.foodName.add(item)
                        adapter.notifyItemInserted(DataManagerFoodName.foodName.size-1)
                    }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    fun goToLogInActivity(view: View){
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is logged in with email and password, navigate to CustomerAccountActivity
            startActivity(Intent(this, CustomerAccountActivity::class.java))
        } else {
            // No user is logged in, navigate to login page
            startActivity(Intent(this, LogInActivity::class.java))
        }
    }

    fun goToShoppingListActivity(view: View) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is logged in with email and password, navigate to ShoppingListActivity
            startActivity(Intent(this, ShoppingListActivity::class.java))
        } else {
            // No user is logged in, navigate to login page
            startActivity(Intent(this, LogInActivity::class.java))
        }
    }

    fun updateLogInTextStatus() {
        // Get the current user
        val user: FirebaseUser? = auth.currentUser

        val logInTextView: TextView = findViewById(R.id.LogInTextView)

        if (user != null) {
            logInTextView.text = getString(R.string.my_account_text)
        } else {
            logInTextView.text = getString(R.string.log_in_text)
        }
    }
}