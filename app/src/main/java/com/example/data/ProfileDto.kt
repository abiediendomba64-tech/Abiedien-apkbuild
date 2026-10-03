package com.example.data

import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val userId: String,
    val displayName: String,
    val role: String,
    val active: Boolean
)
