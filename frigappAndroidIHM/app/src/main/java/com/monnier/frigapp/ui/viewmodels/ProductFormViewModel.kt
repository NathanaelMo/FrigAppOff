package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.FridgeResult
import com.monnier.frigapp.data.repository.ItemResult
import com.monnier.frigapp.generate.model.CreateProductRequest
import com.monnier.frigapp.generate.model.FridgeItemRequest
import com.monnier.frigapp.generate.model.FridgeSummary
import com.monnier.frigapp.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

sealed class ProductFormState {
    object Idle    : ProductFormState()
    object Loading : ProductFormState()
    object Success : ProductFormState()
    data class Error(val message: String) : ProductFormState()
}

class ProductFormViewModel(application: Application) : AndroidViewModel(application) {

    private val fridgeRepository = (application as FrigApplication).fridgeRepository
    private val itemRepository   = (application as FrigApplication).itemRepository

    private val _fridges = MutableStateFlow<List<FridgeSummary>>(emptyList())
    val fridges = _fridges.asStateFlow()

    private val _state = MutableStateFlow<ProductFormState>(ProductFormState.Idle)
    val state = _state.asStateFlow()

    init { loadFridges() }

    private fun loadFridges() {
        viewModelScope.launch {
            when (val result = fridgeRepository.getFridges()) {
                is FridgeResult.Success -> _fridges.value = result.data
                is FridgeResult.Error   -> { /* l'UI gère la liste vide */ }
            }
        }
    }

    /**
     * Ajoute le produit au frigo sélectionné.
     *
     * Flux :
     * 1. Si [productId] est fourni (scan réussi) → addItem directement
     * 2. Si [barcode] est fourni mais pas de productId (produit inconnu) →
     *    POST /products pour le créer, puis addItem
     * 3. Sinon → erreur
     */
    fun addItem(
        productId:     String,
        fridgeId:      String,
        quantity:      Int,
        expiryDateStr: String,
        barcode:       String? = null,
        name:          String? = null
    ) {
        val fridgeUuid = runCatching { UUID.fromString(fridgeId) }.getOrNull() ?: run {
            _state.value = ProductFormState.Error("Sélectionne un frigo")
            return
        }
        val expiryDate = parseDate(expiryDateStr) ?: run {
            _state.value = ProductFormState.Error("Date invalide — format attendu : JJ/MM/AAAA")
            return
        }

        viewModelScope.launch {
            _state.value = ProductFormState.Loading

            // Résoudre le productId
            val resolvedProductId: UUID? = when {
                // 1. On a déjà un ID (Scan réussi ou produit existant)
                productId.isNotBlank() -> runCatching { UUID.fromString(productId) }.getOrNull()

                // 2. Produit inconnu avec code-barres
                !barcode.isNullOrBlank() -> createProductAndGetId(barcode, name ?: barcode)

                // 3 : Saisie manuelle pure (Ni ID, ni barcode)
                !name.isNullOrBlank() -> {
                    // Génération d'un barcode unique pour respecter la contrainte DB
                    val technicalBarcode = "MAN-${UUID.randomUUID().toString().take(8)}"
                    createProductAndGetId(technicalBarcode, name)
                }
                else -> null
            }

            if (resolvedProductId == null) {
                _state.value = ProductFormState.Error("Produit invalide ou création impossible")
                return@launch
            }

            val request = FridgeItemRequest(
                productId  = resolvedProductId,
                quantity   = quantity,
                expiryDate = expiryDate
            )
            when (val result = itemRepository.addItem(fridgeUuid, request)) {
                is ItemResult.Success -> _state.value = ProductFormState.Success
                is ItemResult.Error   -> _state.value = ProductFormState.Error(result.message)
            }
        }
    }

    fun resetState() { _state.value = ProductFormState.Idle }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Crée un produit manuel via POST /products et retourne son UUID. */
    private suspend fun createProductAndGetId(barcode: String, name: String): UUID? {
        return try {
            val response = ApiService.productsApi.productsPost(
                CreateProductRequest(barcode = barcode, name = name)
            )
            Log.d("ProductForm", "Réponse: ${response}")

            if (response.isSuccessful) {
                // CRUCIAL : Extraire l'ID du corps de la réponse "data"
                val newProduct = response.body()?.data
                Log.d("ProductForm", "Produit créé avec ID: ${newProduct?.id}")
                newProduct?.id
            } else {
                Log.e("ProductForm", "Erreur API création: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ProductForm", "Exception création produit", e)
            null
        }
    }

    /** Accepte "JJ/MM/AAAA" ou "JJ / MM / AAAA". */
    private fun parseDate(input: String): LocalDate? {
        val cleaned = input.replace(" ", "").replace("-", "/")
        return listOf("dd/MM/yyyy", "d/M/yyyy").firstNotNullOfOrNull { pattern ->
            runCatching {
                LocalDate.parse(cleaned, DateTimeFormatter.ofPattern(pattern))
            }.getOrNull()
        }
    }
}
