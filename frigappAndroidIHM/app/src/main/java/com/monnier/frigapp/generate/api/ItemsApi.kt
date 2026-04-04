package com.monnier.frigapp.generate.api

import com.monnier.frigapp.generate.model.FridgeItemRequest
import com.monnier.frigapp.generate.model.FridgeItemUpdateRequest
import com.monnier.frigapp.generate.model.FridgesFridgeIdItemsGet200Response
import com.monnier.frigapp.generate.model.FridgesFridgeIdItemsPost201Response
import retrofit2.http.*
import retrofit2.Response

import com.squareup.moshi.Json


interface ItemsApi {

    /**
    * enum for parameter sort
    */
    enum class SortFridgesFridgeIdItemsGet(val value: kotlin.String) {
        @Json(name = "expiry_asc") expiry_asc("expiry_asc"),
        @Json(name = "expiry_desc") expiry_desc("expiry_desc"),
        @Json(name = "name_asc") name_asc("name_asc")
    }

    /**
     * Lister les items du frigo
     * Retourne les items triés par date de péremption. Inclut un flag urgent pour les produits expirant dans 3 jours ou moins.
     * Responses:
     *  - 200: Liste des items
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Ressource introuvable
     *
     * @param fridgeId 
     * @param sort Ordre de tri (optional, default to expiry_asc)
     * @return [FridgesFridgeIdItemsGet200Response]
     */
    @GET("fridges/{fridgeId}/items")
    suspend fun fridgesFridgeIdItemsGet(@Path("fridgeId") fridgeId: java.util.UUID, @Query("sort") sort: SortFridgesFridgeIdItemsGet? = SortFridgesFridgeIdItemsGet.expiry_asc): Response<FridgesFridgeIdItemsGet200Response>

    /**
     * Supprimer un item
     * Retire un item du frigo. Déclenche une notification WebSocket ITEM_DELETED.
     * Responses:
     *  - 204: Item supprimé
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Ressource introuvable
     *
     * @param fridgeId 
     * @param itemId 
     * @return [Unit]
     */
    @DELETE("fridges/{fridgeId}/items/{itemId}")
    suspend fun fridgesFridgeIdItemsItemIdDelete(@Path("fridgeId") fridgeId: java.util.UUID, @Path("itemId") itemId: java.util.UUID): Response<Unit>

    /**
     * Modifier un item
     * Modifie la DLC et/ou la quantité. Déclenche une notification WebSocket ITEM_UPDATED.
     * Responses:
     *  - 200: Item modifié
     *  - 400: Données invalides
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Ressource introuvable
     *
     * @param fridgeId 
     * @param itemId 
     * @param fridgeItemUpdateRequest 
     * @return [FridgesFridgeIdItemsPost201Response]
     */
    @PUT("fridges/{fridgeId}/items/{itemId}")
    suspend fun fridgesFridgeIdItemsItemIdPut(@Path("fridgeId") fridgeId: java.util.UUID, @Path("itemId") itemId: java.util.UUID, @Body fridgeItemUpdateRequest: FridgeItemUpdateRequest): Response<FridgesFridgeIdItemsPost201Response>

    /**
     * Ajouter un item au frigo
     * Ajoute un produit au frigo. Déclenche une notification WebSocket ITEM_ADDED à tous les membres.
     * Responses:
     *  - 201: Item ajouté
     *  - 400: Données invalides
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Ressource introuvable
     *
     * @param fridgeId 
     * @param fridgeItemRequest 
     * @return [FridgesFridgeIdItemsPost201Response]
     */
    @POST("fridges/{fridgeId}/items")
    suspend fun fridgesFridgeIdItemsPost(@Path("fridgeId") fridgeId: java.util.UUID, @Body fridgeItemRequest: FridgeItemRequest): Response<FridgesFridgeIdItemsPost201Response>

}
