package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.FridgeResult
import kotlinx.coroutines.launch

/**
 * ViewModel de l'écran de création d'un frigo.
 */
class FridgeCreateViewModel(application: Application) : AndroidViewModel(application) {

    private val fridgeRepository = (application as FrigApplication).fridgeRepository

    // ─── États des champs ─────────────────────────────────────────────────────

    var name         by mutableStateOf("")
    var isLoading    by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // ─── Actions ─────────────────────────────────────────────────────────────

    /**
     * Crée le frigo via l'API.
     * En cas de succès, [onSuccess] est appelé pour fermer l'écran.
     */
    fun createFridge(onSuccess: () -> Unit) {
        if (name.isBlank()) {
            errorMessage = "Le nom du frigo est obligatoire"
            return
        }

        viewModelScope.launch {
            isLoading    = true
            errorMessage = null

            when (val result = fridgeRepository.createFridge(name.trim())) {
                is FridgeResult.Success -> onSuccess()
                is FridgeResult.Error   -> errorMessage = result.message
            }

            isLoading = false
        }
    }
}
