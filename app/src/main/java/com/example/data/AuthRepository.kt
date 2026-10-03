package com.example.data

import io.github.jan.supabase.auth.providers.Email

class AuthRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    suspend fun currentProfile(): ProfileDto? {
        val user = supabase.auth.currentUserOrNull() ?: return null

        return supabase
            .from("profiles")
            .select {
                filter {
                    eq("user_id", user.id)
                    eq("active", true)
                }
            }
            .decodeList<ProfileDto>().firstOrNull()
    }
}
