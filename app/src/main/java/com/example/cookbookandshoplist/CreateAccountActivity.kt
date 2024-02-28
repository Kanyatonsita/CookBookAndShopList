package com.example.cookbookandshoplist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccountActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val createAccountButton: Button = findViewById(R.id.CreateAccountButton)
        createAccountButton.setOnClickListener {
            createUserAccount()
        }
    }

    fun createUserAccount() {
        val firstName = findViewById<EditText>(R.id.firstNameEditText).text.toString()
        val lastName = findViewById<EditText>(R.id.lastNameEditText).text.toString()
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast(getString(R.string.fill_all_fields_message))
            return
        }

        if (!isValidEmail(email)) {
            findViewById<EditText>(R.id.editTextEmail).error = getString(R.string.invalid_mail_message)
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->

                val firebaseUser = auth.currentUser

                if (task.isSuccessful){
                    val user = User(firstName, lastName)
                    if (firebaseUser != null) {
                        saveUserToFirestore(user, firebaseUser.uid)
                        showToast(getString(R.string.save_user_message))
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }else{
                    showToast(getString(R.string.save_user_error_message))
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun saveUserToFirestore(user: User, uid: String?) {
        if (uid != null) {

            firestore.collection("Users")
                .document(uid)
                .set(user)
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->
                    // Handle errors while saving user data to Firestore
                    showToast(getString(R.string.save_user_data_error_message))
                }
        }
    }

   private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}