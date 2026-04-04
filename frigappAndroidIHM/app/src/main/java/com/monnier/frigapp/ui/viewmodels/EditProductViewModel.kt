package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.ItemResult
import com.monnier.frigapp.generate.model.FridgeItem
import com.monnier.frigapp.generate.model.FridgeItemUpdateRequest
import com.monnier.frigapp.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

sealed class EditProductState {
    object Idle    : EditProductState()
    object Loading : EditProductState()
    object Saved   : EditProductState()
    object Deleted : EditProductState()
    data class Error(val message: String) : EditProductState()
}

class EditProductViewModel(application: Application) : AndroidViewModel(application) {

    private val itemRepository = (application as FrigApplication).itemRepository

    private val _item  = MutableStateFlow<FridgeItem?>(null)
    val item = _item.asStateFlow()

    private val _state = MutableStateFlow<EditProductState>(EditProductState.Idle)
    val state = _state.asStateFlow()

    private var fridgeUuid:   UUID? = null
    private var itemUuid:     UUID? = null
    private var productUuid:  UUID? = null

    fun load(fridgeId: String, itemId: String, productId: String = "") {
        fridgeUuid  = runCatching { UUID.fromString(fridgeId) }.getOrNull()
        itemUuid    = runCatching { UUID.fromString(itemId) }.getOrNull()
        productUuid = runCatching { UUID.fromString(productId) }.getOrNull()
        // Les données de l'item viennent du FridgeViewModel déjà chargé.
        // On stocke juste les UUIDs pour les appels API.
    }

    fun saveChanges(quantity: Int, expiryDateStr: String) {
        val fridgeId = fridgeUuid ?: run {
            _state.value = EditProductState.Error("Frigo invalide")
            return
        }
        val itemId = itemUuid ?: run {
            _state.value = EditProductState.Error("Item invalide")
            return
        }
        val expiryDate = parseDate(expiryDateStr) ?: run {
            _state.value = EditProductState.Error("Date invalide — format attendu : JJ/MM/AAAA")
            return
        }

        viewModelScope.launch {
            _state.value = EditProductState.Loading
            val request = FridgeItemUpdateRequest(quantity = quantity, expiryDate = expiryDate)
            when (val result = itemRepository.updateItem(fridgeId, itemId, request)) {
                is ItemResult.Success -> _state.value = EditProductState.Saved
                is ItemResult.Error   -> _state.value = EditProductState.Error(result.message)
            }
        }
    }

    fun deleteItem() {
        val fridgeId = fridgeUuid ?: return
        val itemId   = itemUuid   ?: return

        viewModelScope.launch {
            _state.value = EditProductState.Loading
            when (val result = itemRepository.deleteItem(fridgeId, itemId)) {
                is ItemResult.Success -> {
                    tryDeleteManualProduct()
                    _state.value = EditProductState.Deleted
                }
                is ItemResult.Error   -> _state.value = EditProductState.Error(result.message)
            }
        }
    }

    private suspend fun tryDeleteManualProduct() {
        val productId = productUuid ?: return
        try {
            ApiService.productsApi.productsProductIdDelete(productId)
        } catch (e: Exception) {
            Log.d("EditProductViewModel", "Suppression produit ignorée: ${e.message}")
        }
    }

    fun resetState() { _state.value = EditProductState.Idle }

    private fun parseDate(input: String): LocalDate? {
        val cleaned = input.replace(" ", "").replace("-", "/")
        return listOf("dd/MM/yyyy", "d/M/yyyy").firstNotNullOfOrNull { pattern ->
            runCatching {
                LocalDate.parse(cleaned, DateTimeFormatter.ofPattern(pattern))
            }.getOrNull()
        }
    }
}
