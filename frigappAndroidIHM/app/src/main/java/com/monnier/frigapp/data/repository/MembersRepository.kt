package com.monnier.frigapp.data.repository

import com.monnier.frigapp.generate.model.InviteMemberRequest
import com.monnier.frigapp.generate.model.MemberSummary
import com.monnier.frigapp.network.ApiService
import java.util.UUID

sealed class MembersResult<out T> {
    data class Success<T>(val data: T) : MembersResult<T>()
    data class Error(val message: String) : MembersResult<Nothing>()
}

class MembersRepository {

    private val api = ApiService.membersApi

    suspend fun getMembers(fridgeId: UUID): MembersResult<List<MemberSummary>> {
        return try {
            val response = api.fridgesFridgeIdMembersGet(fridgeId)
            if (response.isSuccessful) {
                MembersResult.Success(response.body()?.data ?: emptyList())
            } else {
                MembersResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            MembersResult.Error("Impossible de charger les membres. Vérifie ta connexion.")
        }
    }

    suspend fun inviteMember(fridgeId: UUID, email: String): MembersResult<MemberSummary> {
        return try {
            val response = api.fridgesFridgeIdMembersInvitePost(
                fridgeId, InviteMemberRequest(email = email)
            )
            if (response.isSuccessful) {
                val member = response.body()?.data
                if (member != null) MembersResult.Success(member)
                else MembersResult.Error("Membre ajouté mais réponse vide")
            } else {
                MembersResult.Error(when (response.code()) {
                    403  -> "Seul le propriétaire peut inviter des membres"
                    404  -> "Aucun compte trouvé pour cet email"
                    409  -> "Cet utilisateur est déjà membre"
                    else -> errorMessage(response.code())
                })
            }
        } catch (e: Exception) {
            MembersResult.Error("Impossible d'inviter le membre. Vérifie ta connexion.")
        }
    }

    suspend fun removeMember(fridgeId: UUID, userId: UUID): MembersResult<Unit> {
        return try {
            val response = api.fridgesFridgeIdMembersUserIdDelete(fridgeId, userId)
            if (response.isSuccessful) {
                MembersResult.Success(Unit)
            } else {
                MembersResult.Error(when (response.code()) {
                    403  -> "Seul le propriétaire peut retirer des membres"
                    else -> errorMessage(response.code())
                })
            }
        } catch (e: Exception) {
            MembersResult.Error("Impossible de retirer le membre. Vérifie ta connexion.")
        }
    }

    private fun errorMessage(code: Int) = when (code) {
        401  -> "Session expirée, reconnecte-toi"
        403  -> "Action non autorisée"
        404  -> "Ressource introuvable"
        else -> "Erreur serveur ($code)"
    }
}
