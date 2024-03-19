package com.example.cookbookandshoplist

import com.google.firebase.firestore.DocumentId

data class User (
    @DocumentId val documentId: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val userProfilePic : String? = null
)