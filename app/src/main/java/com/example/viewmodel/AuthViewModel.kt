package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.ProfileDto
import com.example.data.SupabaseClientProvider
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val supabase = SupabaseClientProvider.client

    val sessionStatus = supabase.auth.sessionStatus

    private val _profile = MutableStateFlow<ProfileDto?>(null)
    val profile: StateFlow<ProfileDto?> = _profile.asStateFlow()

    private val _profileLoaded = MutableStateFlow(false)
    val profileLoaded: StateFlow<Boolean> = _profileLoaded.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private var loadedUserId: String? = null

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authError.value = "Email dan password wajib diisi."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            _profileLoaded.value = false
            try {
                repository.signIn(email.trim(), password)
            } catch (e: Throwable) {
                _authError.value = e.message ?: "Login gagal."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId == null) {
                _profile.value = null
                _profileLoaded.value = true
                loadedUserId = null
                return@launch
            }

            if (_profileLoaded.value && loadedUserId == userId) {
                return@launch
            }

            try {
                val current = repository.currentProfile()
                _profile.value = current
                loadedUserId = userId
                _authError.value = if (current == null) {
                    "Akun terautentikasi, tetapi belum memiliki akses aplikasi."
                } else {
                    null
                }
            } catch (e: Throwable) {
                _profile.value = null
                _authError.value = e.message ?: "Gagal memverifikasi profil akses."
            } finally {
                _profileLoaded.value = true
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
            } finally {
                _profile.value = null
                _profileLoaded.value = false
                loadedUserId = null
            }
        }
    }
}
