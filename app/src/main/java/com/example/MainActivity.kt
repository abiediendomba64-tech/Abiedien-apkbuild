package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AccessDeniedScreen
import com.example.ui.screens.AuthLoadingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuthViewModel
import io.github.jan.supabase.auth.status.SessionStatus

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val sessionStatus by authViewModel.sessionStatus.collectAsStateWithLifecycle(
                    initialValue = SessionStatus.Initializing
                )
                val profile by authViewModel.profile.collectAsStateWithLifecycle()
                val profileLoaded by authViewModel.profileLoaded.collectAsStateWithLifecycle()
                val authError by authViewModel.authError.collectAsStateWithLifecycle()

                LaunchedEffect(sessionStatus) {
                    if (sessionStatus is SessionStatus.Authenticated) {
                        authViewModel.loadProfile()
                    }
                }

                LaunchedEffect(profile?.userId, profile?.active) {
                    if (profile?.active == true) {
                        startActivity(
                            Intent(this@MainActivity, DashboardActivity::class.java)
                        )
                        finish()
                    }
                }

                when (sessionStatus) {
                    SessionStatus.Initializing -> AuthLoadingScreen()
                    is SessionStatus.Authenticated -> {
                        when {
                            profile?.active == true -> AuthLoadingScreen(
                                "Membuka panel aman..."
                            )
                            profileLoaded -> AccessDeniedScreen(
                                message = authError
                                    ?: "Akun belum diprovision untuk aplikasi ini.",
                                onSignOut = { authViewModel.signOut() }
                            )
                            else -> AuthLoadingScreen(
                                "Memverifikasi hak akses akun..."
                            )
                        }
                    }
                    else -> LoginScreen(
                        authViewModel = authViewModel,
                        errorMessage = authError
                    )
                }
            }
        }
    }
}
