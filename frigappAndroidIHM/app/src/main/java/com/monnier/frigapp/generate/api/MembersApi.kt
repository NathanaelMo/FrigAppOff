package com.monnier.frigapp.generate.api


import com.monnier.frigapp.generate.model.FridgesFridgeIdMembersGet200Response
import com.monnier.frigapp.generate.model.FridgesFridgeIdMembersInvitePost201Response
import com.monnier.frigapp.generate.model.InviteMemberRequest
import retrofit2.http.*
import retrofit2.Response


interface MembersApi {
    /**
     * Lister les membres
     * 
     * Responses:
     *  - 200: Liste des membres
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Ressource introuvable
     *
     * @param fridgeId 
     * @return [FridgesFridgeIdMembersGet200Response]
     */
    @GET("fridges/{fridgeId}/members")
    suspend fun fridgesFridgeIdMembersGet(@Path("fridgeId") fridgeId: java.util.UUID): Response<FridgesFridgeIdMembersGet200Response>

    /**
     * Inviter un membre
     * Invite un utilisateur existant par email. Réservé au propriétaire.
     * Responses:
     *  - 201: Membre ajouté
     *  - 400: Données invalides
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Utilisateur introuvable
     *  - 409: Déjà membre
     *
     * @param fridgeId 
     * @param inviteMemberRequest 
     * @return [FridgesFridgeIdMembersInvitePost201Response]
     */
    @POST("fridges/{fridgeId}/members/invite")
    suspend fun fridgesFridgeIdMembersInvitePost(@Path("fridgeId") fridgeId: java.util.UUID, @Body inviteMemberRequest: InviteMemberRequest): Response<FridgesFridgeIdMembersInvitePost201Response>

    /**
     * Retirer un membre
     * Retire un collaborateur du frigo. Réservé au propriétaire.
     * Responses:
     *  - 204: Membre retiré
     *  - 401: Token JWT absent ou invalide
     *  - 403: Action non autorisée pour ce rôle
     *  - 404: Ressource introuvable
     *
     * @param fridgeId 
     * @param userId 
     * @return [Unit]
     */
    @DELETE("fridges/{fridgeId}/members/{userId}")
    suspend fun fridgesFridgeIdMembersUserIdDelete(@Path("fridgeId") fridgeId: java.util.UUID, @Path("userId") userId: java.util.UUID): Response<Unit>

}
