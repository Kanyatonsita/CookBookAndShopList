package com.example.cookbookandshoplist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class CustomerAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_account)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!


        getUserInfo(currentUser.uid)
    }


    fun getUserInfo(uid: String) {
        val db = Firebase.firestore
        val userRef = db.collection("Users").document(uid)

        userRef.addSnapshotListener { document, e ->
            if (document != null && document.exists()) {
                val user = document.toObject<User>()
                if (user != null) {
                    val userNameTextView: TextView = findViewById(R.id.userNameTextView)
                    userNameTextView.text = "${user.firstName} ${user.lastName}"
                } else {
                    Log.e("!!!", getString(R.string.fetch_user_data_error_message)+"$e")
                }
            }

        }
    }


    fun logOut(view: View) {
        auth.signOut()
        // Redirect to the login activity after logout and clear the back stack
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}