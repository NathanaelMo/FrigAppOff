package com.monnier.frigapp.generate.api


import com.monnier.frigapp.generate.model.FridgeRequest
import com.monnier.frigapp.generate.model.FridgesGet200Response
import com.monnier.frigapp.generate.model.FridgesPost201Response
import retrofit2.http.*
import retrofit2.Response


interface FridgesApi {
    /**
     * Supprimer un frigo
     * Supprime le frigo et toutes ses données en cascade. Réservé au propriétaire.
     * Responses:
     *  - 204: Frigo supprimé
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Ressource introuvable
     *
     * @param fridgeId Identifiant du frigo
     * @return [Unit]
     */
    @DELETE("fridges/{fridgeId}")
    suspend fun fridgesFridgeIdDelete(@Path("fridgeId") fridgeId: java.util.UUID): Response<Unit>

    /**
     * Détail d&#39;un frigo
     * 
     * Responses:
     *  - 200: Détail du frigo
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Ressource introuvable
     *
     * @param fridgeId Identifiant du frigo
     * @return [FridgesPost201Response]
     */
    @GET("fridges/{fridgeId}")
    suspend fun fridgesFridgeIdGet(@Path("fridgeId") fridgeId: java.util.UUID): Response<FridgesPost201Response>

    /**
     * Quitter un frigo
     * Retire l&#39;utilisateur courant des membres du frigo. Réservé aux collaborateurs.
     * Responses:
     *  - 204: Frigo quitté
     *  - 401: Token JWT absent ou invalide
     *  - 403: Le propriétaire ne peut pas quitter son propre frigo
     *  - 404: Ressource introuvable
     *
     * @param fridgeId 
     * @return [Unit]
     */
    @DELETE("fridges/{fridgeId}/leave")
    suspend fun fridgesFridgeIdLeaveDelete(@Path("fridgeId") fridgeId: java.util.UUID): Response<Unit>

    /**
     * Renommer un frigo
     * Réservé au propriétaire du frigo.
     * Responses:
     *  - 200: Frigo renommé
     *  - 400: Données invalides
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Ressource introuvable
     *
     * @param fridgeId Identifiant du frigo
     * @param fridgeRequest 
     * @return [FridgesPost201Response]
     */
    @PUT("fridges/{fridgeId}")
    suspend fun fridgesFridgeIdPut(@Path("fridgeId") fridgeId: java.util.UUID, @Body fridgeRequest: FridgeRequest): Response<FridgesPost201Response>

    /**
     * Lister mes frigos
     * Retourne tous les frigos auxquels l&#39;utilisateur appartient (possédés + collaborations).
     * Responses:
     *  - 200: Liste des frigos
     *  - 401: Token JWT absent ou invalide
     *
     * @return [FridgesGet200Response]
     */
    @GET("fridges")
    suspend fun fridgesGet(): Response<FridgesGet200Response>

    /**
     * Créer un frigo
     * Crée un nouveau frigo. L&#39;utilisateur courant devient automatiquement propriétaire.
     * Responses:
     *  - 201: Frigo créé
     *  - 400: Données invalides
     *  - 401: Token JWT absent ou invalide
     *
     * @param fridgeRequest 
     * @return [FridgesPost201Response]
     */
    @POST("fridges")
    suspend fun fridgesPost(@Body fridgeRequest: FridgeRequest): Response<FridgesPost201Response>

}
