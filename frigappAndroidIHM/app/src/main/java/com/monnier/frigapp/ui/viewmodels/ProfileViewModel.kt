package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.FridgeResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de l'écran profil.
 *
 * Charge :
 * - Le prénom et l'email depuis [TokenRepository] (persisté au login, disponible hors ligne)
 * - Les statistiques (nb frigos, nb produits totaux, nb membres totaux) via [FridgeRepository]
 *   en sommant les compteurs retournés par GET /fridges
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenRepository  = (application as FrigApplication).tokenRepository
    private val fridgeRepository = (application as FrigApplication).fridgeRepository

    // ─── Données utilisateur ─────────────────────────────────────────────────

    private val _userName  = MutableStateFlow("")
    val userName  = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    // ─── Statistiques ────────────────────────────────────────────────────────

    private val _fridgeCount  = MutableStateFlow(0)
    val fridgeCount  = _fridgeCount.asStateFlow()

    private val _productCount = MutableStateFlow(0)
    val productCount = _productCount.asStateFlow()

    private val _memberCount  = MutableStateFlow(0)
    val memberCount  = _memberCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ─── Initialisation ───────────────────────────────────────────────────────

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Données locales (DataStore) — instantanées
            _userName.value  = tokenRepository.getUserName()  ?: ""
            _userEmail.value = tokenRepository.getUserEmail() ?: ""

            // 2. Statistiques via l'API
            when (val result = fridgeRepository.getFridges()) {
                is FridgeResult.Success -> {
                    val fridges = result.data
                    _fridgeCount.value  = fridges.size
                    // Somme des itemCount et memberCount de chaque frigo
                    _productCount.value = fridges.sumOf { it.itemCount   ?: 0 }
                    _memberCount.value  = fridges.sumOf { it.memberCount ?: 0 }
                }
                is FridgeResult.Error -> {
                    // En cas d'erreur réseau, on affiche 0 — pas bloquant
                    _fridgeCount.value  = 0
                    _productCount.value = 0
                    _memberCount.value  = 0
                }
            }

            _isLoading.value = false
        }
    }
}
