package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.FridgeResult
import com.monnier.frigapp.generate.model.FridgeSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de l'écran liste des frigos.
 *
 * Charge tous les frigos de l'utilisateur via [FridgeRepository].
 * Supporte le pull-to-refresh via [loadFridges].
 */
class FridgeListViewModel(application: Application) : AndroidViewModel(application) {

    private val fridgeRepository = (application as FrigApplication).fridgeRepository

    // ─── États exposés ────────────────────────────────────────────────────────

    private val _fridges = MutableStateFlow<List<FridgeSummary>>(emptyList())
    val fridges = _fridges.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // ─── Initialisation ───────────────────────────────────────────────────────

    init {
        loadFridges()
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    /**
     * Charge (ou recharge) la liste des frigos depuis l'API.
     * Appelé automatiquement au démarrage et sur pull-to-refresh.
     */
    fun loadFridges() {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null

            when (val result = fridgeRepository.getFridges()) {
                is FridgeResult.Success -> _fridges.value = result.data
                is FridgeResult.Error   -> _errorMessage.value = result.message
            }

            _isLoading.value = false
        }
    }
}
