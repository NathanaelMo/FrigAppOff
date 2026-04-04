package com.monnier.frigapp.generate.api


import com.monnier.frigapp.generate.model.AuthLoginPost200Response
import com.monnier.frigapp.generate.model.AuthRegisterPost201Response
import retrofit2.http.*
import retrofit2.Response



import com.monnier.frigapp.generate.model.LoginRequest
import com.monnier.frigapp.generate.model.RegisterRequest

interface AuthApi {
    /**
     * Connexion
     * Authentifie un utilisateur et retourne un token JWT.
     * Responses:
     *  - 200: Connexion réussie
     *  - 400: Données invalides
     *  - 401: Identifiants incorrects
     *
     * @param loginRequest 
     * @return [AuthLoginPost200Response]
     */
    @POST("auth/login")
    suspend fun authLoginPost(@Body loginRequest: LoginRequest): Response<AuthLoginPost200Response>

    /**
     * Rafraîchir le token
     * Génère un nouveau token JWT à partir d&#39;un token valide.
     * Responses:
     *  - 200: Token renouvelé
     *  - 401: Token JWT absent ou invalide
     *
     * @return [AuthLoginPost200Response]
     */
    @POST("auth/refresh")
    suspend fun authRefreshPost(): Response<AuthLoginPost200Response>

    /**
     * Créer un compte
     * Inscrit un nouvel utilisateur. Retourne le profil créé.
     * Responses:
     *  - 201: Compte créé avec succès
     *  - 400: Données invalides
     *  - 409: Email déjà utilisé
     *
     * @param registerRequest 
     * @return [AuthRegisterPost201Response]
     */
    @POST("auth/register")
    suspend fun authRegisterPost(@Body registerRequest: RegisterRequest): Response<AuthRegisterPost201Response>

}
