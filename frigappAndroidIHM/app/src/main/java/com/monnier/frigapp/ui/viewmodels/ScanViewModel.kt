package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.remote.ScannedProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── États du scan ────────────────────────────────────────────────────────────

/**
 * Représente les différents états possibles de l'écran de scan.
 */
sealed class ScanState {
    /** Caméra active, en attente d'un code-barres. */
    object Scanning : ScanState()

    /** Code-barres détecté, recherche en cours via le backend. */
    object Loading : ScanState()

    /** Produit trouvé (via cache local ou Open Food Facts via le backend). */
    data class ProductFound(val product: ScannedProduct) : ScanState()

    /**
     * Code-barres inconnu partout (base + OFF).
     * → formulaire manuel avec l'EAN pré-rempli.
     */
    data class ProductNotFound(val barcode: String) : ScanState()

    /** Erreur inattendue (perte réseau totale, serveur down…). */
    data class Error(val message: String) : ScanState()
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

/**
 * ViewModel de l'écran de scan.
 *
 * Flux :
 * 1. ML Kit détecte un EAN → [onBarcodeDetected]
 * 2. Appel au backend : GET /products/barcode/{barcode}
 *    Le backend cherche en base (cache), puis sur Open Food Facts si nécessaire
 * 3. → [ScanState.ProductFound] si le produit est connu
 * 4. → [ScanState.ProductNotFound] si introuvable partout (404)
 * 5. ScanScreen observe l'état et navigue vers le formulaire produit
 * 6. [resetScan] remet l'état à [ScanState.Scanning] après navigation
 */
class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository = (application as FrigApplication).productRepository

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Scanning)
    val scanState = _scanState.asStateFlow()

    /**
     * Appelé par [BarcodeScannerAnalyzer] à chaque détection de code-barres.
     *
     * Ignoré si un scan est déjà en cours (évite les appels en double).
     */
    fun onBarcodeDetected(barcode: String) {
        // Guard : on n'accepte un nouveau scan que si on est dans l'état Scanning
        if (_scanState.value !is ScanState.Scanning) return

        viewModelScope.launch {
            _scanState.value = ScanState.Loading

            val product = productRepository.getByBarcode(barcode)

            _scanState.value = if (product != null && product.name.isNotBlank()) {
                ScanState.ProductFound(product)
            } else {
                // 404 = produit inconnu → l'utilisateur saisira manuellement
                ScanState.ProductNotFound(barcode)
            }
        }
    }

    /**
     * Remet le scanner en mode actif après une navigation ou une annulation.
     */
    fun resetScan() {
        _scanState.value = ScanState.Scanning
    }
}
