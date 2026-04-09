package com.monnier.frigapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monnier.frigapp.data.remote.ScannedProduct
import com.monnier.frigapp.ui.camera.BarcodeScannerAnalyzer
import com.monnier.frigapp.ui.viewmodels.ScanState
import com.monnier.frigapp.ui.viewmodels.ScanViewModel

@Composable
fun ScanScreen(
    onManualEntryClick: () -> Unit,
    /** Appelé quand un produit est trouvé sur Open Food Facts. */
    onProductScanned: (ScannedProduct) -> Unit = {},
    /** Appelé quand le barcode est inconnu — formulaire manuel avec EAN pré-rempli. */
    onBarcodeNotFound: (String) -> Unit = {},
    viewModel: ScanViewModel = viewModel()
) {
    val context       = LocalContext.current
    val scanState     by viewModel.scanState.collectAsState()

    // ─── Gestion de la permission caméra ─────────────────────────────────────

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedPermanently by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted        = granted
        permissionDeniedPermanently    = !granted
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ─── Navigation après détection ──────────────────────────────────────────

    LaunchedEffect(scanState) {
        when (val state = scanState) {
            is ScanState.ProductFound    -> {
                onProductScanned(state.product)
                viewModel.resetScan()
            }
            is ScanState.ProductNotFound -> {
                onBarcodeNotFound(state.barcode)
                viewModel.resetScan()
            }
            else -> { /* Scanning / Loading / Error → rien à faire */ }
        }
    }

    // ─── Animation ligne de scan ─────────────────────────────────────────────

    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val translateY by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 200f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lineTranslation"
    )

    // ─── UI ──────────────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Barre de titre ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                Text(
                    text       = "Scanner un produit",
                    color      = Color.White,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Viseur caméra ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                when {
                    // Permission accordée ET scan actif → flux caméra réel
                    cameraPermissionGranted && scanState is ScanState.Scanning -> {
                        CameraPreview(
                            modifier           = Modifier.fillMaxSize(),
                            onBarcodeDetected  = { viewModel.onBarcodeDetected(it) }
                        )
                    }
                    // Code-barres détecté : caméra arrêtée, chargement en cours
                    cameraPermissionGranted && scanState is ScanState.Loading -> {
                        CircularProgressIndicator(
                            color    = Color(0xFF2EAA84),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    // Permission refusée définitivement → message
                    permissionDeniedPermanently -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint     = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Permission caméra refusée.\nAutorise l'accès dans les paramètres de l'app.",
                                color    = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                    // En attente de la permission
                    else -> {
                        CircularProgressIndicator(color = Color(0xFF2EAA84))
                    }
                }

                // Coins du viseur (par-dessus la caméra)
                ScannerCorners(color = Color(0xFF2EAA84))

                // Ligne de scan animée
                if (cameraPermissionGranted && scanState is ScanState.Scanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(2.dp)
                            .offset(y = (translateY - 100).dp)
                            .background(Color(0xFF2EAA84))
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Instruction / état ────────────────────────────────────────
            when (scanState) {
                is ScanState.Scanning -> Text(
                    "Placez le code-barres dans le cadre",
                    color    = Color.Gray,
                    fontSize = 14.sp
                )
                is ScanState.Loading  -> Text(
                    "Recherche du produit…",
                    color    = Color(0xFF2EAA84),
                    fontSize = 14.sp
                )
                else -> { /* ProductFound / ProductNotFound → navigation déjà déclenchée */ }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Saisie manuelle ───────────────────────────────────────────
            TextButton(
                onClick  = onManualEntryClick,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    "Saisir manuellement",
                    color      = Color(0xFF2EAA84),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── Composants internes ──────────────────────────────────────────────────────

@Composable
fun ScannerCorners(
    color: Color,
    cornerLength: Dp = 24.dp,
    strokeWidth: Dp  = 4.dp
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val sw     = strokeWidth.toPx()
        val corner = cornerLength.toPx()
        val pad    = 20.dp.toPx()
        val cap    = StrokeCap.Round

        drawLine(color, Offset(pad, pad + corner),              Offset(pad, pad),                         sw, cap)
        drawLine(color, Offset(pad, pad),                       Offset(pad + corner, pad),                sw, cap)
        drawLine(color, Offset(size.width - pad - corner, pad), Offset(size.width - pad, pad),            sw, cap)
        drawLine(color, Offset(size.width - pad, pad),          Offset(size.width - pad, pad + corner),   sw, cap)
        drawLine(color, Offset(pad, size.height - pad - corner),Offset(pad, size.height - pad),           sw, cap)
        drawLine(color, Offset(pad, size.height - pad),         Offset(pad + corner, size.height - pad),  sw, cap)
        drawLine(color, Offset(size.width - pad - corner, size.height - pad), Offset(size.width - pad, size.height - pad), sw, cap)
        drawLine(color, Offset(size.width - pad, size.height - pad - corner), Offset(size.width - pad, size.height - pad), sw, cap)
    }
}

// ─── Composable CameraX ───────────────────────────────────────────────────────

/**
 * Prévisualisation du flux caméra avec analyse ML Kit en temps réel.
 *
 * Utilise [AndroidView] pour intégrer [PreviewView] (View Android) dans Compose.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory  = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Prévisualisation caméra
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Analyse ML Kit — stratégie KEEP_ONLY_LATEST pour ne pas accumuler les frames
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            ContextCompat.getMainExecutor(ctx),
                            BarcodeScannerAnalyzer(onBarcodeDetected)
                        )
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}
