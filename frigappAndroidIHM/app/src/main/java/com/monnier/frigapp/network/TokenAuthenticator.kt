package com.monnier.frigapp.network

import com.monnier.frigapp.data.local.TokenRepository
import com.monnier.frigapp.generate.infrastructure.Serializer
import com.monnier.frigapp.generate.model.AuthLoginPost200Response
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

/**
 * Intercepteur OkHttp appelé automatiquement sur chaque réponse 401.
 *
 * Flux :
 * 1. La requête originale retourne 401 (token expiré)
 * 2. OkHttp appelle [authenticate] sur le thread IO
 * 3. On appelle POST /auth/refresh via un client séparé (évite la récursion infinie)
 * 4. Succès → on sauvegarde le nouveau token et on rejoue la requête originale
 * 5. Échec  → on vide le token (DataStore + mémoire) → AppNavigation redirige vers login
 */
class TokenAuthenticator(
    private val tokenRepository: TokenRepository,
    private val baseUrl: String
) : Authenticator {

    // Client minimal sans Authenticator ni intercepteurs pour éviter toute récursion
    private val refreshClient = OkHttpClient()

    private val moshiAdapter = Serializer.moshi.adapter(AuthLoginPost200Response::class.java)

    override fun authenticate(route: Route?, response: Response): Request? {
        // Si c'est déjà le refresh qui a échoué → on s'arrête
        if (response.request.url.pathSegments.contains("refresh")) return null

        val newToken = runBlocking { performRefresh() } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private suspend fun performRefresh(): String? {
        return try {
            val currentToken = tokenRepository.getToken() ?: return null

            val request = Request.Builder()
                .url("${baseUrl}auth/refresh")
                .addHeader("Authorization", "Bearer $currentToken")
                .post(ByteArray(0).toRequestBody())
                .build()

            val okResponse = refreshClient.newCall(request).execute()

            if (okResponse.isSuccessful) {
                val bodyStr = okResponse.body?.string() ?: return null
                val newToken = moshiAdapter.fromJson(bodyStr)?.data?.token

                if (newToken != null) {
                    tokenRepository.saveAuthData(
                        token     = newToken,
                        userId    = tokenRepository.getUserId()    ?: "",
                        userName  = tokenRepository.getUserName()  ?: "",
                        userEmail = tokenRepository.getUserEmail() ?: ""
                    )
                    ApiService.setToken(newToken)
                }
                newToken
            } else {
                // Refresh échoué → déconnexion forcée
                tokenRepository.clearAuthData()
                ApiService.clearToken()
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
