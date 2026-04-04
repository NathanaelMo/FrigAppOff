package com.monnier.frigapp.data.repository

import com.monnier.frigapp.generate.model.FridgeFull
import com.monnier.frigapp.generate.model.FridgeRequest
import com.monnier.frigapp.generate.model.FridgeSummary
import com.monnier.frigapp.network.ApiService
import java.util.UUID

// ─── Résultat générique des opérations frigo ─────────────────────────────────

/**
 * Sealed class représentant le résultat d'une opération sur les frigos.
 * Même pattern qu'[AuthResult] pour une gestion d'erreur exhaustive.
 */
sealed class FridgeResult<out T> {
    data class Success<T>(val data: T) : FridgeResult<T>()
    data class Error<T>(val message: String) : FridgeResult<T>()
}

// ─── Repository ──────────────────────────────────────────────────────────────

/**
 * Repository central pour toutes les opérations sur les frigos.
 *
 * Endpoints couverts :
 * - GET  /fridges                    → liste de tous les frigos de l'utilisateur
 * - POST /fridges                    → créer un frigo
 * - GET  /fridges/{id}               → détails d'un frigo
 * - PUT  /fridges/{id}               → renommer un frigo (owner uniquement)
 * - DELETE /fridges/{id}             → supprimer un frigo (owner uniquement)
 * - DELETE /fridges/{id}/leave       → quitter un frigo (collaborator uniquement)
 *
 * Les items (produits dans le frigo) seront gérés dans [ItemRepository]
 * quand le backend sera prêt.
 */
class FridgeRepository {

    // ─── Liste des frigos ─────────────────────────────────────────────────────

    /**
     * Récupère tous les frigos de l'utilisateur connecté
     * (frigos possédés + frigos en collaboration).
     */
    suspend fun getFridges(): FridgeResult<List<FridgeSummary>> {
        return try {
            val response = ApiService.fridgesApi.fridgesGet()

            if (response.isSuccessful) {
                val fridges = response.body()?.data ?: emptyList()
                FridgeResult.Success(fridges)
            } else {
                val message = when (response.code()) {
                    401  -> "Session expirée, reconnecte-toi"
                    else -> "Erreur serveur (${response.code()})"
                }
                FridgeResult.Error(message)
            }
        } catch (e: Exception) {
            FridgeResult.Error("Impossible de charger les frigos. Vérifie ta connexion.")
        }
    }

    // ─── Détail d'un frigo ────────────────────────────────────────────────────

    /**
     * Récupère les détails complets d'un frigo (nom, rôle, compteurs).
     *
     * @param fridgeId UUID du frigo
     */
    suspend fun getFridge(fridgeId: UUID): FridgeResult<FridgeFull> {
        return try {
            val response = ApiService.fridgesApi.fridgesFridgeIdGet(fridgeId)

            if (response.isSuccessful) {
                val fridge = response.body()?.data
                if (fridge != null) {
                    FridgeResult.Success(fridge)
                } else {
                    FridgeResult.Error("Données du frigo introuvables")
                }
            } else {
                val message = when (response.code()) {
                    401  -> "Session expirée, reconnecte-toi"
                    403  -> "Accès non autorisé à ce frigo"
                    404  -> "Ce frigo n'existe plus"
                    else -> "Erreur serveur (${response.code()})"
                }
                FridgeResult.Error(message)
            }
        } catch (e: Exception) {
            FridgeResult.Error("Impossible de charger le frigo. Vérifie ta connexion.")
        }
    }

    // ─── Créer un frigo ───────────────────────────────────────────────────────

    /**
     * Crée un nouveau frigo. L'utilisateur devient automatiquement propriétaire.
     *
     * @param name Nom du frigo (1-100 caractères)
     */
    suspend fun createFridge(name: String): FridgeResult<FridgeFull> {
        return try {
            val response = ApiService.fridgesApi.fridgesPost(FridgeRequest(name = name))

            if (response.isSuccessful) {
                val fridge = response.body()?.data
                if (fridge != null) {
                    FridgeResult.Success(fridge)
                } else {
                    FridgeResult.Error("Frigo créé mais données absentes de la réponse")
                }
            } else {
                val message = when (response.code()) {
                    400  -> "Nom invalide (1 à 100 caractères)"
                    401  -> "Session expirée, reconnecte-toi"
                    else -> "Erreur lors de la création (${response.code()})"
                }
                FridgeResult.Error(message)
            }
        } catch (e: Exception) {
            FridgeResult.Error("Impossible de créer le frigo. Vérifie ta connexion.")
        }
    }

    // ─── Renommer un frigo ────────────────────────────────────────────────────

    /**
     * Renomme un frigo existant. Réservé au propriétaire.
     *
     * @param fridgeId UUID du frigo
     * @param newName  Nouveau nom (1-100 caractères)
     */
    suspend fun renameFridge(fridgeId: UUID, newName: String): FridgeResult<FridgeFull> {
        return try {
            val response = ApiService.fridgesApi.fridgesFridgeIdPut(
                fridgeId     = fridgeId,
                fridgeRequest = FridgeRequest(name = newName)
            )

            if (response.isSuccessful) {
                val fridge = response.body()?.data
                if (fridge != null) {
                    FridgeResult.Success(fridge)
                } else {
                    FridgeResult.Error("Frigo renommé mais données absentes de la réponse")
                }
            } else {
                val message = when (response.code()) {
                    400  -> "Nom invalide (1 à 100 caractères)"
                    401  -> "Session expirée, reconnecte-toi"
                    403  -> "Seul le propriétaire peut renommer ce frigo"
                    404  -> "Ce frigo n'existe plus"
                    else -> "Erreur lors du renommage (${response.code()})"
                }
                FridgeResult.Error(message)
            }
        } catch (e: Exception) {
            FridgeResult.Error("Impossible de renommer le frigo. Vérifie ta connexion.")
        }
    }

    // ─── Supprimer un frigo ───────────────────────────────────────────────────

    /**
     * Supprime un frigo et toutes ses données en cascade. Réservé au propriétaire.
     *
     * @param fridgeId UUID du frigo
     */
    suspend fun deleteFridge(fridgeId: UUID): FridgeResult<Unit> {
        return try {
            val response = ApiService.fridgesApi.fridgesFridgeIdDelete(fridgeId)

            if (response.isSuccessful) {
                FridgeResult.Success(Unit)
            } else {
                val message = when (response.code()) {
                    401  -> "Session expirée, reconnecte-toi"
                    403  -> "Seul le propriétaire peut supprimer ce frigo"
                    404  -> "Ce frigo n'existe plus"
                    else -> "Erreur lors de la suppression (${response.code()})"
                }
                FridgeResult.Error(message)
            }
        } catch (e: Exception) {
            FridgeResult.Error("Impossible de supprimer le frigo. Vérifie ta connexion.")
        }
    }

    // ─── Quitter un frigo ─────────────────────────────────────────────────────

    /**
     * Retire l'utilisateur courant des membres du frigo.
     * Réservé aux collaborateurs (les propriétaires ne peuvent pas quitter).
     *
     * @param fridgeId UUID du frigo
     */
    suspend fun leaveFridge(fridgeId: UUID): FridgeResult<Unit> {
        return try {
            val response = ApiService.fridgesApi.fridgesFridgeIdLeaveDelete(fridgeId)

            if (response.isSuccessful) {
                FridgeResult.Success(Unit)
            } else {
                val message = when (response.code()) {
                    401  -> "Session expirée, reconnecte-toi"
                    403  -> "Le propriétaire ne peut pas quitter son propre frigo"
                    404  -> "Ce frigo n'existe plus"
                    else -> "Erreur lors de la désinscription (${response.code()})"
                }
                FridgeResult.Error(message)
            }
        } catch (e: Exception) {
            FridgeResult.Error("Impossible de quitter le frigo. Vérifie ta connexion.")
        }
    }
}
