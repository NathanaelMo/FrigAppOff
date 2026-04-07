package com.monnier.frigapp.network

import com.monnier.frigapp.data.local.TokenRepository
import com.monnier.frigapp.generate.api.AuthApi
import com.monnier.frigapp.generate.api.FridgesApi
import com.monnier.frigapp.generate.api.ItemsApi
import com.monnier.frigapp.generate.api.MembersApi
import com.monnier.frigapp.generate.api.ProductsApi
import com.monnier.frigapp.generate.infrastructure.ApiClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Singleton central pour tous les appels API de FrigApp.
 *
 * Architecture :
 * - [AuthInterceptor] : injecte le JWT Bearer token dans chaque requête
 * - [HttpLoggingInterceptor] : logs réseau (désactivé en release via BuildConfig)
 * - [ApiClient] : Retrofit client généré par OpenAPI, configuré avec notre OkHttpClient
 *
 * Utilisation :
 *   ApiService.authApi.authLoginPost(...)
 *   ApiService.setToken(token)   ← après un login réussi
 *   ApiService.clearToken()      ← après un logout
 */
object ApiService {

    private const val BASE_URL = "https://frigappoff.onrender.com/"

    // ─── Intercepteur de token (mutable, partagé) ────────────────────────────

    /**
     * Intercepteur qui injecte le header Authorization: Bearer <token>.
     * Exposé en interne pour que les repositories puissent le mettre à jour.
     */
    private val authInterceptor = AuthInterceptor()

    /** Authenticator pour le refresh automatique du token JWT. Initialisé via [setup]. */
    private var tokenAuthenticator: TokenAuthenticator? = null

    // ─── Initialisation ──────────────────────────────────────────────────────

    /**
     * À appeler une seule fois depuis [FrigApplication.onCreate] avant tout appel API.
     * Injecte le [TokenRepository] dans le [TokenAuthenticator].
     */
    fun setup(tokenRepository: TokenRepository) {
        tokenAuthenticator = TokenAuthenticator(tokenRepository, BASE_URL)
    }

    // ─── Client OkHttp ───────────────────────────────────────────────────────

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // 1. Token JWT — doit être en premier pour être appliqué à chaque requête
            .addInterceptor(authInterceptor)
            // 2. Logs réseau (à désactiver en release avec BuildConfig.DEBUG)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            // 3. Refresh automatique sur 401
            .apply { tokenAuthenticator?.let { authenticator(it) } }
            .build()
    }

    // ─── Client Retrofit (généré par OpenAPI) ────────────────────────────────

    private val apiClient = ApiClient(
        baseUrl = BASE_URL,
        okHttpClientBuilder = okHttpClient.newBuilder()
    )

    // ─── Services exposés ────────────────────────────────────────────────────

    /** Endpoints d'authentification : /auth/login, /auth/register, /auth/refresh */
    val authApi: AuthApi by lazy { apiClient.createService(AuthApi::class.java) }

    /** Endpoints des frigos : /fridges, /fridges/{id} */
    val fridgesApi: FridgesApi by lazy { apiClient.createService(FridgesApi::class.java) }

    /** Endpoints des produits dans un frigo : /fridges/{id}/items */
    val itemsApi: ItemsApi by lazy { apiClient.createService(ItemsApi::class.java) }

    /** Endpoints des membres d'un frigo : /fridges/{id}/members */
    val membersApi: MembersApi by lazy { apiClient.createService(MembersApi::class.java) }

    /** Endpoints du référentiel produits : /products/barcode/{barcode}, /products */
    val productsApi: ProductsApi by lazy { apiClient.createService(ProductsApi::class.java) }

    // ─── Gestion du token ────────────────────────────────────────────────────

    /**
     * Injecte le token JWT dans l'intercepteur.
     * À appeler après un login ou un refresh réussi.
     */
    fun setToken(token: String) {
        authInterceptor.token = token
    }

    /**
     * Efface le token JWT de l'intercepteur.
     * À appeler après un logout.
     */
    fun clearToken() {
        authInterceptor.token = ""
    }
}
