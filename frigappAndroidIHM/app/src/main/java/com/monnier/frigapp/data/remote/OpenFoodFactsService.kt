package com.monnier.frigapp.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// ─── Modèles de réponse Open Food Facts ──────────────────────────────────────

@JsonClass(generateAdapter = true)
data class OFFProductRaw(
    @Json(name = "product_name_fr") val productNameFr: String? = null,
    @Json(name = "product_name")    val productName: String?   = null,
    @Json(name = "brands")          val brands: String?         = null,
    @Json(name = "image_front_url") val imageUrl: String?       = null,
    @Json(name = "quantity")        val quantity: String?        = null
)

@JsonClass(generateAdapter = true)
data class OFFResponse(
    @Json(name = "status")  val status: Int            = 0, // 1 = trouvé, 0 = introuvable
    @Json(name = "product") val product: OFFProductRaw? = null
)

// ─── Modèle exposé au ViewModel ──────────────────────────────────────────────

/**
 * Données d'un produit retournées après un scan, prêtes à afficher dans le formulaire.
 */
data class ScannedProduct(
    val barcode:  String,
    val name:     String,
    val brand:    String? = null,
    val imageUrl: String? = null,
    val quantity: String? = null,
    /** true = données trouvées sur Open Food Facts ; false = produit inconnu */
    val foundOnOFF: Boolean = false,
    /** UUID du produit en base (rempli quand la recherche passe par le backend). */
    val productId: String? = null
)

// ─── Interface Retrofit ───────────────────────────────────────────────────────

private interface OpenFoodFactsApi {
    /**
     * Recherche un produit par son code EAN.
     * Seuls les champs nécessaires sont demandés pour limiter la taille de la réponse.
     */
    @GET("api/v0/product/{barcode}.json?fields=product_name,product_name_fr,brands,image_front_url,quantity")
    suspend fun getProduct(@Path("barcode") barcode: String): Response<OFFResponse>
}

// ─── Service singleton ────────────────────────────────────────────────────────

/**
 * Client Open Food Facts.
 *
 * Expose une seule méthode [searchByBarcode] qui :
 * - Appelle l'API publique Open Food Facts
 * - Retourne un [ScannedProduct] si le produit est trouvé
 * - Retourne un [ScannedProduct] minimal (barcode only) si inconnu
 * - Ne lève jamais d'exception visible (toutes les erreurs sont absorbées)
 */
object OpenFoodFactsService {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val api: OpenFoodFactsApi = Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OpenFoodFactsApi::class.java)

    /**
     * Recherche un produit par code EAN.
     *
     * @param barcode Code EAN scanné par ML Kit
     * @return [ScannedProduct] avec [ScannedProduct.foundOnOFF] = true si trouvé,
     *         ou un objet minimal avec juste le barcode si introuvable
     */
    suspend fun searchByBarcode(barcode: String): ScannedProduct {
        return try {
            val response = api.getProduct(barcode)

            if (response.isSuccessful && response.body()?.status == 1) {
                val raw = response.body()?.product
                ScannedProduct(
                    barcode    = barcode,
                    // Préférence pour le nom français
                    name       = raw?.productNameFr?.takeIf { it.isNotBlank() }
                        ?: raw?.productName?.takeIf { it.isNotBlank() }
                        ?: "",
                    // Prend uniquement la première marque si plusieurs
                    brand      = raw?.brands?.split(",")?.firstOrNull()?.trim()?.takeIf { it.isNotBlank() },
                    imageUrl   = raw?.imageUrl,
                    quantity   = raw?.quantity,
                    foundOnOFF = true
                )
            } else {
                // Produit non référencé dans OFF → formulaire manuel avec barcode pré-rempli
                ScannedProduct(barcode = barcode, name = "", foundOnOFF = false)
            }
        } catch (e: Exception) {
            // Erreur réseau → on laisse l'utilisateur saisir manuellement
            ScannedProduct(barcode = barcode, name = "", foundOnOFF = false)
        }
    }
}
