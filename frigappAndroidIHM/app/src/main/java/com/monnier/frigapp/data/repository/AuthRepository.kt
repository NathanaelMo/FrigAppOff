package com.monnier.frigapp.data.repository

import com.monnier.frigapp.data.local.TokenRepository
import com.monnier.frigapp.generate.model.LoginRequest
import com.monnier.frigapp.generate.model.RegisterRequest
import com.monnier.frigapp.network.ApiService

// ─── Résultat générique des opérations auth ──────────────────────────────────

/**
 * Sealed class représentant le résultat d'une opération d'authentification.
 * Permet une gestion d'erreur exhaustive côté ViewModel (when sans else).
 */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error<T>(val message: String) : AuthResult<T>()
}

// ─── Repository ──────────────────────────────────────────────────────────────

/**
 * Repository central pour toutes les opérations d'authentification.
 *
 * Responsabilités :
 * - Appeler l'API (AuthApi via ApiService)
 * - Persister / supprimer le JWT (TokenRepository)
 * - Injecter / effacer le token dans l'intercepteur HTTP (ApiService)
 *
 * Ne contient aucune logique UI — uniquement de la logique métier.
 */
class AuthRepository(private val tokenRepository: TokenRepository) {

    // ─── Login ───────────────────────────────────────────────────────────────

    /**
     * Authentifie l'utilisateur.
     *
     * En cas de succès :
     * 1. Sauvegarde le token + infos user dans DataStore
     * 2. Injecte le token dans l'intercepteur pour les prochaines requêtes
     *
     * @param email    Email saisi
     * @param password Mot de passe saisi
     * @return [AuthResult.Success] ou [AuthResult.Error] avec message lisible
     */
    suspend fun login(email: String, password: String): AuthResult<Unit> {
        return try {
            val response = ApiService.authApi.authLoginPost(
                LoginRequest(email = email, password = password)
            )

            if (response.isSuccessful) {
                val authData = response.body()?.data
                val token = authData?.token

                if (token != null) {
                    // 1. Persistance locale
                    tokenRepository.saveAuthData(
                        token     = token,
                        userId    = authData.user?.id?.toString() ?: "",
                        userName  = authData.user?.firstName ?: "",
                        userEmail = authData.user?.email ?: ""
                    )
                    // 2. Injection immédiate dans l'intercepteur HTTP
                    ApiService.setToken(token)

                    AuthResult.Success(Unit)
                } else {
                    AuthResult.Error("Token absent dans la réponse serveur")
                }
            } else {
                val errorMsg = when (response.code()) {
                    400  -> "Email ou mot de passe invalide"
                    401  -> "Identifiants incorrects"
                    500  -> "Erreur serveur, réessaie plus tard"
                    else -> "Erreur inattendue (code ${response.code()})"
                }
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de joindre le serveur. Vérifie ta connexion.")
        }
    }

    // ─── Register ────────────────────────────────────────────────────────────

    /**
     * Crée un nouveau compte utilisateur.
     *
     * Après une inscription réussie, l'utilisateur devra se connecter
     * manuellement (pas d'auto-login pour respecter la confirmation côté serveur).
     *
     * @param firstName Prénom saisi
     * @param email     Email saisi
     * @param password  Mot de passe saisi
     * @return [AuthResult.Success] ou [AuthResult.Error] avec message lisible
     */
    suspend fun register(firstName: String, email: String, password: String): AuthResult<Unit> {
        return try {
            val response = ApiService.authApi.authRegisterPost(
                RegisterRequest(firstName = firstName, email = email, password = password)
            )

            if (response.isSuccessful) {
                AuthResult.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    400  -> "Données invalides (vérifie le format de l'email)"
                    409  -> "Cet email est déjà associé à un compte"
                    500  -> "Erreur serveur, réessaie plus tard"
                    else -> "Erreur inattendue (code ${response.code()})"
                }
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de joindre le serveur. Vérifie ta connexion.")
        }
    }

    // ─── Logout ──────────────────────────────────────────────────────────────

    /**
     * Déconnecte l'utilisateur :
     * - Supprime les données persistées (DataStore)
     * - Efface le token en mémoire (intercepteur HTTP)
     */
    suspend fun logout() {
        tokenRepository.clearAuthData()
        ApiService.clearToken()
    }

    // ─── Restauration au démarrage ───────────────────────────────────────────

    /**
     * Restaure le token depuis DataStore vers l'intercepteur HTTP.
     * À appeler au démarrage de l'app si l'utilisateur était déjà connecté.
     */
    suspend fun restoreToken() {
        val token = tokenRepository.getToken()
        if (!token.isNullOrBlank()) {
            ApiService.setToken(token)
        }
    }

    /**
     * Vérifie si un token valide (non vide) existe en local.
     * Utilisé par AppNavigation pour décider la destination de démarrage.
     */
    suspend fun isLoggedIn(): Boolean {
        return !tokenRepository.getToken().isNullOrBlank()
    }

    // ─── Refresh token ───────────────────────────────────────────────────────

    /**
     * Renouvelle le JWT via l'endpoint /auth/refresh.
     * À appeler depuis un intercepteur OkHttp quand l'API retourne 401.
     *
     * @return [AuthResult.Success] avec le nouveau token, ou [AuthResult.Error]
     */
    suspend fun refreshToken(): AuthResult<String> {
        return try {
            val response = ApiService.authApi.authRefreshPost()

            if (response.isSuccessful) {
                val newToken = response.body()?.data?.token

                if (newToken != null) {
                    // On met à jour la persistance et l'intercepteur
                    val userId    = tokenRepository.getUserId() ?: ""
                    val userName  = tokenRepository.getUserName() ?: ""
                    val userEmail = tokenRepository.getUserEmail() ?: ""

                    tokenRepository.saveAuthData(newToken, userId, userName, userEmail)
                    ApiService.setToken(newToken)

                    AuthResult.Success(newToken)
                } else {
                    AuthResult.Error("Token absent dans la réponse de refresh")
                }
            } else {
                // 401 sur le refresh = session expirée → forcer re-login
                logout()
                AuthResult.Error("Session expirée, veuillez vous reconnecter")
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de renouveler la session")
        }
    }
}
