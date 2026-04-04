package com.monnier.frigapp.data.repository

import com.monnier.frigapp.generate.model.FridgeItem
import com.monnier.frigapp.generate.model.FridgeItemRequest
import com.monnier.frigapp.generate.model.FridgeItemUpdateRequest
import com.monnier.frigapp.network.ApiService
import java.util.UUID

sealed class ItemResult<out T> {
    data class Success<T>(val data: T) : ItemResult<T>()
    data class Error(val message: String) : ItemResult<Nothing>()
}

class ItemRepository {

    private val api = ApiService.itemsApi

    suspend fun getItems(fridgeId: UUID): ItemResult<List<FridgeItem>> {
        return try {
            val response = api.fridgesFridgeIdItemsGet(fridgeId)
            if (response.isSuccessful) {
                ItemResult.Success(response.body()?.data ?: emptyList())
            } else {
                ItemResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            ItemResult.Error("Impossible de charger les produits. Vérifie ta connexion.")
        }
    }

    suspend fun addItem(fridgeId: UUID, request: FridgeItemRequest): ItemResult<FridgeItem> {
        return try {
            val response = api.fridgesFridgeIdItemsPost(fridgeId, request)
            if (response.isSuccessful) {
                val item = response.body()?.data
                if (item != null) ItemResult.Success(item)
                else ItemResult.Error("Produit ajouté mais réponse vide")
            } else {
                ItemResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            ItemResult.Error("Impossible d'ajouter le produit. Vérifie ta connexion.")
        }
    }

    suspend fun updateItem(
        fridgeId: UUID,
        itemId: UUID,
        request: FridgeItemUpdateRequest
    ): ItemResult<FridgeItem> {
        return try {
            val response = api.fridgesFridgeIdItemsItemIdPut(fridgeId, itemId, request)
            if (response.isSuccessful) {
                val item = response.body()?.data
                if (item != null) ItemResult.Success(item)
                else ItemResult.Error("Produit modifié mais réponse vide")
            } else {
                ItemResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            ItemResult.Error("Impossible de modifier le produit. Vérifie ta connexion.")
        }
    }

    suspend fun deleteItem(fridgeId: UUID, itemId: UUID): ItemResult<Unit> {
        return try {
            val response = api.fridgesFridgeIdItemsItemIdDelete(fridgeId, itemId)
            if (response.isSuccessful) {
                ItemResult.Success(Unit)
            } else {
                ItemResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            ItemResult.Error("Impossible de supprimer le produit. Vérifie ta connexion.")
        }
    }

    private fun errorMessage(code: Int) = when (code) {
        401  -> "Session expirée, reconnecte-toi"
        403  -> "Action non autorisée"
        404  -> "Ressource introuvable"
        else -> "Erreur serveur ($code)"
    }
}
