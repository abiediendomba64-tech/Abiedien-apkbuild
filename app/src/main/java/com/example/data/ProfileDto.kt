package com.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    val role: String,
    val active: Boolean
)
