package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.AuthResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel de l'écran d'inscription.
 *
 * Hérite de [AndroidViewModel] pour accéder à [FrigApplication]
 * et récupérer [AuthRepository] sans injection de dépendances externe.
 */
class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = (application as FrigApplication).authRepository

    // ─── États des champs ─────────────────────────────────────────────────────

    var name     by mutableStateOf("")
    var email    by mutableStateOf("")
    var password by mutableStateOf("")

    // ─── États de l'UI ───────────────────────────────────────────────────────

    var isLoading    by mutableStateOf(false)
    var isRegistered by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    /** Force du mot de passe de 0 (vide) à 4 (fort). Utilisé pour la barre visuelle. */
    val passwordStrength: Int
        get() = when {
            password.length > 8 -> 4
            password.length > 5 -> 3
            password.length > 2 -> 2
            password.isNotEmpty() -> 1
            else -> 0
        }

    // ─── Actions ─────────────────────────────────────────────────────────────

    /**
     * Déclenche l'inscription :
     * 1. Validation locale (champs vides)
     * 2. Appel API via AuthRepository
     * 3. En cas de succès → affiche le message de confirmation 2s puis [onSuccess]
     * 4. En cas d'erreur → affichage du message dans [errorMessage]
     */
    fun onRegisterClick(onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Tous les champs sont obligatoires"
            return
        }

        viewModelScope.launch {
            isLoading    = true
            errorMessage = null

            when (val result = authRepository.register(name.trim(), email.trim(), password)) {
                is AuthResult.Success -> {
                    isRegistered = true
                    delay(2000) // Laisse le temps à l'utilisateur de lire le message de succès
                    onSuccess()
                }
                is AuthResult.Error -> errorMessage = result.message
            }

            isLoading = false
        }
    }
}
