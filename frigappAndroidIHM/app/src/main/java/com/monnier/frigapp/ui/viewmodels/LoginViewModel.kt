package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.AuthResult
import kotlinx.coroutines.launch

/**
 * ViewModel de l'écran de connexion.
 *
 * Hérite de [AndroidViewModel] pour accéder à [FrigApplication]
 * et récupérer [AuthRepository] sans injection de dépendances externe.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = (application as FrigApplication).authRepository

    // ─── États des champs ─────────────────────────────────────────────────────

    var email    by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)

    // ─── États de l'UI ───────────────────────────────────────────────────────

    var isLoading    by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // ─── Actions ─────────────────────────────────────────────────────────────

    /**
     * Déclenche le login :
     * 1. Validation locale (champs vides)
     * 2. Appel API via AuthRepository
     * 3. En cas de succès → callback [onSuccess] pour naviguer vers l'app
     * 4. En cas d'erreur → affichage du message dans [errorMessage]
     */
    fun onLoginClick(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Veuillez remplir tous les champs"
            return
        }

        viewModelScope.launch {
            isLoading    = true
            errorMessage = null

            when (val result = authRepository.login(email.trim(), password)) {
                is AuthResult.Success -> onSuccess()
                is AuthResult.Error   -> errorMessage = result.message
            }

            isLoading = false
        }
    }

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }
}
