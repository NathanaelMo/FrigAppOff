package com.monnier.frigapp.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Intercepteur OkHttp qui injecte le JWT Bearer token dans chaque requête HTTP.
 *
 * Fonctionne avec un token mutable en mémoire :
 * - Appelé via ApiService.setToken(token) après un login réussi
 * - Appelé via ApiService.clearToken() après un logout
 *
 * Si le token est vide, l'en-tête Authorization n'est PAS ajouté
 * (les endpoints publics comme /auth/login fonctionnent normalement).
 */
class AuthInterceptor : Interceptor {

    /** Token JWT en mémoire. Mis à jour par ApiService. */
    @Volatile
    var token: String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // On n'écrase pas si un header Authorization est déjà présent
        if (token.isBlank() || originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
