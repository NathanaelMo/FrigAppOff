package com.monnier.frigapp.ui.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * Analyseur d'images CameraX qui détecte les codes-barres via ML Kit.
 *
 * Utilisé dans [ImageAnalysis.setAnalyzer] pour traiter chaque frame de la caméra.
 *
 * Comportement :
 * - Traite un seul frame à la fois (flag [isProcessing]) pour éviter la surcharge
 * - Appelle [onBarcodeDetected] dès qu'un EAN/QR est détecté
 * - La déduplication (éviter de rappeler pour le même code) est gérée par [ScanViewModel]
 */
class BarcodeScannerAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    /** Empêche le traitement simultané de plusieurs frames. */
    @Volatile
    private var isProcessing = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // Si un frame est déjà en cours d'analyse, on ignore celui-ci
        if (isProcessing) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        isProcessing = true

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                // On prend le premier code-barres trouvé avec une valeur non vide
                barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                    ?.rawValue
                    ?.let { onBarcodeDetected(it) }
            }
            .addOnCompleteListener {
                // Toujours libérer le proxy pour recevoir le frame suivant
                isProcessing = false
                imageProxy.close()
            }
    }
}
