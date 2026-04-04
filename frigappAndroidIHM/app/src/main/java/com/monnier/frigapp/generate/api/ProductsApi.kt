package com.monnier.frigapp.generate.api


import com.monnier.frigapp.generate.model.CreateProductRequest
import com.monnier.frigapp.generate.model.ProductsBarcodeBarcodeGet200Response
import retrofit2.http.*
import retrofit2.Response


interface ProductsApi {
    /**
     * Rechercher un produit par code-barres
     * Cherche dans la base locale, puis sur Open Food Facts si introuvable. Met le résultat en cache.
     * Responses:
     *  - 200: Produit trouvé
     *  - 401: Token JWT absent ou invalide
     *  - 404: Produit introuvable
     *  - 502: Erreur Open Food Facts
     *
     * @param barcode Code-barres EAN-13
     * @return [ProductsBarcodeBarcodeGet200Response]
     */
    @GET("products/barcode/{barcode}")
    suspend fun productsBarcodeBarcodeGet(@Path("barcode") barcode: kotlin.String): Response<ProductsBarcodeBarcodeGet200Response>

    /**
     * Créer un produit manuellement
     * Crée un produit dans le référentiel local quand le scan ne donne aucun résultat.
     * Responses:
     *  - 201: Produit créé
     *  - 400: Données invalides
     *  - 401: Token JWT absent ou invalide
     *  - 409: Code-barres déjà existant
     *
     * @param createProductRequest 
     * @return [ProductsBarcodeBarcodeGet200Response]
     */
    @POST("products")
    suspend fun productsPost(@Body createProductRequest: CreateProductRequest): Response<ProductsBarcodeBarcodeGet200Response>

    /**
     * Supprimer un produit manuel
     * Supprime un produit uniquement s'il a été créé manuellement (source=manual) et n'est plus référencé.
     * Responses:
     *  - 204: Produit supprimé
     *  - 401: Token JWT absent ou invalide
     *  - 403: Le produit n'est pas de source manuelle
     *  - 404: Produit introuvable
     *  - 409: Produit encore référencé par des items
     *
     * @param productId UUID du produit
     * @return [Unit]
     */
    @DELETE("products/{productId}")
    suspend fun productsProductIdDelete(@Path("productId") productId: java.util.UUID): Response<Unit>

}
