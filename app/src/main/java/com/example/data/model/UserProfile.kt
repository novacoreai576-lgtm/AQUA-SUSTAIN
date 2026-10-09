package com.example.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
