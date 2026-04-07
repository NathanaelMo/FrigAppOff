package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.FridgeResult
import com.monnier.frigapp.data.repository.ItemResult
import com.monnier.frigapp.generate.model.FridgeFull
import com.monnier.frigapp.generate.model.FridgeItem
import java.time.format.DateTimeFormatter
import com.monnier.frigapp.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Modèle d'affichage d'un item dans le frigo.
 * Découplé du modèle API [FridgeItem] pour simplifier les composables.
 */
data class FridgeItemDisplay(
    val id: String,
    val productId: String,
    val name: String,
    val brand: String?,
    val imageUrl: String?,
    val quantity: Int,
    val expiryDate: String,
    val daysRemaining: Int,
    val isUrgent: Boolean
)

/**
 * ViewModel de l'écran détail d'un frigo.
 */
class FridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val fridgeRepository = (application as FrigApplication).fridgeRepository
    private val itemRepository   = (application as FrigApplication).itemRepository

    // ─── Infos du frigo ───────────────────────────────────────────────────────

    private val _fridgeInfo = MutableStateFlow<FridgeFull?>(null)
    val fridgeInfo = _fridgeInfo.asStateFlow()

    var fridgeName by mutableStateOf("Chargement...")
        private set

    var userRole by mutableStateOf("collaborator")
        private set

    // ─── Items ────────────────────────────────────────────────────────────────

    private val _items = MutableStateFlow<List<FridgeItemDisplay>>(emptyList())
    val products = _items.asStateFlow()   // nom "products" conservé pour ne pas casser FridgeScreen

    // ─── États de chargement ──────────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // ─── Identifiant courant ──────────────────────────────────────────────────

    private var currentFridgeId: UUID? = null

    // ─── Actions ──────────────────────────────────────────────────────────────

    fun fetchFridgeData(fridgeId: String) {
        val uuid = runCatching { UUID.fromString(fridgeId) }.getOrNull() ?: run {
            _errorMessage.value = "Identifiant de frigo invalide"
            _isLoading.value    = false
            return
        }
        currentFridgeId = uuid

        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null

            // 1. Infos du frigo
            when (val result = fridgeRepository.getFridge(uuid)) {
                is FridgeResult.Success -> {
                    _fridgeInfo.value = result.data
                    fridgeName = result.data.name ?: "Mon Frigo"
                    userRole   = result.data.role?.value ?: "collaborator"
                }
                is FridgeResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value    = false
                    return@launch
                }
            }

            // 2. Items du frigo
            when (val result = itemRepository.getItems(uuid)) {
                is ItemResult.Success -> _items.value = result.data.map { it.toDisplay() }
                is ItemResult.Error   -> _errorMessage.value = result.message
            }

            _isLoading.value = false
        }
    }

    fun deleteProduct(itemId: String, productId: String) {
        val fridgeId = currentFridgeId ?: return
        val uuid     = runCatching { UUID.fromString(itemId) }.getOrNull() ?: return

        viewModelScope.launch {
            // Optimistic update
            _items.value = _items.value.filter { it.id != itemId }
            when (val result = itemRepository.deleteItem(fridgeId, uuid)) {
                is ItemResult.Success -> {
                    tryDeleteManualProduct(productId)
                }
                is ItemResult.Error   -> {
                    _errorMessage.value = result.message
                    // Rechargement pour resynchroniser
                    fetchFridgeData(fridgeId.toString())
                }
            }
        }
    }

    private suspend fun tryDeleteManualProduct(productId: String) {
        val productUuid = runCatching { UUID.fromString(productId) }.getOrNull() ?: return
        try {
            ApiService.productsApi.productsProductIdDelete(productUuid)
        } catch (e: Exception) {
            Log.d("FridgeViewModel", "Suppression produit ignorée: ${e.message}")
        }
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private fun FridgeItem.toDisplay() = FridgeItemDisplay(
        id            = id?.toString() ?: "",
        productId     = product?.id?.toString() ?: "",
        name          = product?.name ?: "Produit inconnu",
        brand         = product?.brand,
        imageUrl      = product?.imageUrl?.toString(),
        quantity      = quantity ?: 1,
        expiryDate    = expiryDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "",
        daysRemaining = daysUntilExpiry ?: 0,
        isUrgent      = urgent ?: false
    )
}
