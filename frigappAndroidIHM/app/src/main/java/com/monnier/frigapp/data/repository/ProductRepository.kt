package com.monnier.frigapp.data.repository

import com.monnier.frigapp.data.remote.ScannedProduct
import com.monnier.frigapp.generate.model.Product
import com.monnier.frigapp.network.ApiService

/**
 * Repository d'accès au référentiel de produits.
 *
 * Délègue tous les appels au backend (GET /products/barcode/{barcode}).
 * Le backend se charge de :
 *   1. Chercher dans sa base locale (cache)
 *   2. Si absent : appeler Open Food Facts et sauvegarder le résultat
 *
 * Cela permet de :
 *   - Centraliser la logique produit côté serveur
 *   - Constituer un cache partagé entre tous les utilisateurs
 *   - Ne pas exposer les appels OFF depuis le mobile
 */
class ProductRepository {

    private val api = ApiService.productsApi

    /**
     * Recherche un produit par son code EAN.
     *
     * @param barcode Code EAN-13 scanné
     * @return [ScannedProduct] avec foundOnOFF = true si les données viennent d'Open Food Facts,
     *         ou null si le produit est introuvable (404 du backend)
     */
    suspend fun getByBarcode(barcode: String): ScannedProduct? {
        return try {
            val response = api.productsBarcodeBarcodeGet(barcode)

            if (response.isSuccessful) {
                val product = response.body()?.data ?: return null
                product.toScannedProduct(barcode)
            } else {
                // 404 = produit inconnu partout (base + OFF) → formulaire manuel
                null
            }
        } catch (e: Exception) {
            // Erreur réseau ou serveur inaccessible
            null
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private fun Product.toScannedProduct(fallbackBarcode: String) = ScannedProduct(
        barcode    = this.barcode ?: fallbackBarcode,
        name       = this.name   ?: "",
        brand      = this.brand,
        imageUrl   = this.imageUrl?.toString(),
        foundOnOFF = this.source == Product.Source.open_food_facts,
        productId  = this.id?.toString()
    )
}
