package com.example.cookbookandshoplist

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage

class EditCustomerProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var dowLoadImageView: ImageView
    private var imageUri: Uri? = null

    val storageRef = Firebase.storage.reference.child("profileImages")
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_customer_profile)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        editTextFirstName = findViewById(R.id.newFirstNameEditText)
        editTextLastName = findViewById(R.id.newLastnameEditText)
        dowLoadImageView = findViewById(R.id.dowLoadImageView)

        loadUserInformation(currentUser.uid)

        val saveButton: Button = findViewById(R.id.saveNewProfileButton)
        saveButton.setOnClickListener {
            saveNewProfile(currentUser.uid)
        }

        dowLoadImageView.setOnClickListener{
            resultLauncher.launch("image/*")
        }
    }

    // to let the user chose an image from mobiles internal storage
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()){
        imageUri = it
        dowLoadImageView.setImageURI(it)
    }

    /* Updates user in Users collection
    with profile picture uploaded
    from storage in Uri format. */
    private fun sendUriToUserDoc(currentUser : FirebaseUser, profilePicUri : ProfilePic) {
        db.collection("Users").whereEqualTo("uid", currentUser.uid).get().addOnSuccessListener { documents ->
            for (document in documents) {
                Log.d("uid_query", "${document.id} => ${document.data}")
                val userDocId = document.toObject<User>().documentId

                db.collection("Users").document(userDocId)
                    .update("profilePic", profilePicUri.profileImage)
                Log.d("user_pic", "Profile pic with Uri ${profilePicUri.toString()} added to user.")

            }
        }
            .addOnFailureListener { e ->
                Log.d("uid_query", "Uid not found.")
            }
    }

    // to upload the image to dataBase.
    private fun uploadImage(){
        val storageRef = storageRef.child(System.currentTimeMillis().toString())
        imageUri?.let {
            storageRef.putFile(it).addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val profilePicUri = ProfilePic(uri.toString())
                        db.collection("profile Image").document(currentUser!!.uid).set(profilePicUri)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()
                            }
                        sendUriToUserDoc(currentUser, profilePicUri)
                        setResult(Activity.RESULT_OK)
                        finish() // Finish the activity after uploading the image
                    }
                }
                else{
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserInformation(uid: String) {
        try {
            val db = Firebase.firestore
            val userRef = db.collection("Users").document(uid)

            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject<User>()
                    if (user != null) {
                        // Pre-fill EditText fields with user information
                        editTextFirstName.setText(user.firstName)
                        editTextLastName.setText(user.lastName)
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("EditProfile", getString(R.string.fetch_user_data_error_message)+": $e")
            }
        } catch (e: Exception) {
            Log.e("EditProfile", getString(R.string.load_user_information_error_message)+"$e")
        }
    }

    private fun saveNewProfile(uid: String) {
        val db = Firebase.firestore
        val userRef = db.collection("Users").document(uid)

        val newFirstName = editTextFirstName.text.toString().trim()
        val newLastName = editTextLastName.text.toString().trim()

        if (newFirstName.isEmpty() || newLastName.isEmpty()){
            showToast(getString(R.string.fill_all_fields_message))
            return
        }else{
            // Update user information in Firestore
            userRef.update(
                "firstName", newFirstName,
                "lastName", newLastName
            ).addOnSuccessListener {
                // Successfully updated user information
                Toast.makeText(this, getString(R.string.profile_updated_success_message), Toast.LENGTH_SHORT).show()
                uploadImage()
            }.addOnFailureListener { e ->
                Log.e("EditProfile", getString(R.string.update_user_data_error_message)+"$e")
                // Handle the failure case if needed
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


data class ProfilePic(val profileImage : String? = null)