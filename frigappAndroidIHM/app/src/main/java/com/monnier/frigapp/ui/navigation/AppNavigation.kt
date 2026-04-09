package com.monnier.frigapp.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.ui.screens.EditProductScreen
import com.monnier.frigapp.ui.screens.FridgeCreateScreen
import com.monnier.frigapp.ui.screens.FridgeListScreen
import com.monnier.frigapp.ui.screens.FridgeMembersScreen
import com.monnier.frigapp.ui.screens.FridgeScreen
import com.monnier.frigapp.ui.screens.FridgeSettingsScreen
import com.monnier.frigapp.ui.screens.LoginScreen
import com.monnier.frigapp.ui.screens.ProductFormScreen
import com.monnier.frigapp.ui.screens.ProfileScreen
import com.monnier.frigapp.ui.screens.RegisterScreen
import com.monnier.frigapp.ui.screens.ScanScreen
import android.net.Uri
import kotlinx.coroutines.launch

// ─── Helpers d'encodage URL ───────────────────────────────────────────────────
//
// On utilise android.net.Uri.encode() au lieu de URLEncoder/URLDecoder.
// URLEncoder encode les espaces en "+" et les "%" en "%25".
// NavController décode ensuite "%25" → "%" mais laisse "+" intact.
// Quand notre code appelait ensuite URLDecoder, il voyait un "%" solitaire
// (ex: "100%") et lançait une IllegalArgumentException → crash.
//
// Uri.encode() encode les espaces en "%20" et "%" en "%25".
// NavController décode tout via Uri.getQueryParameter() → valeur déjà décodée dans le Bundle.
// decodeArg() retourne donc la valeur telle quelle (décodage déjà fait par NavController).

private fun String?.encodeArg(): String =
    if (isNullOrBlank()) "" else Uri.encode(this)

private fun String?.decodeArg(): String =
    this ?: ""

// ─── Navigation racine ────────────────────────────────────────────────────────

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()
    val context           = LocalContext.current
    val application       = context.applicationContext as FrigApplication
    val scope             = rememberCoroutineScope()

    // Auto-login : si un token est persisté, on saute directement vers l'app
    LaunchedEffect(Unit) {
        if (application.authRepository.isLoggedIn()) {
            application.authRepository.restoreToken()
            rootNavController.navigate("main_app") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Redirection automatique vers login si le token est vidé (refresh échoué / session expirée)
    LaunchedEffect(Unit) {
        var wasLoggedIn = false
        application.tokenRepository.tokenFlow.collect { token ->
            val isLoggedIn = !token.isNullOrBlank()
            if (wasLoggedIn && !isLoggedIn) {
                rootNavController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
            wasLoggedIn = isLoggedIn
        }
    }

    NavHost(navController = rootNavController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess  = {
                    rootNavController.navigate("main_app") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { rootNavController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(onBackToLogin = { rootNavController.popBackStack() })
        }

        composable("main_app") {
            MainScreenContainer(
                onLogout = {
                    scope.launch { application.authRepository.logout() }
                    rootNavController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

// ─── Conteneur principal avec bottom nav ─────────────────────────────────────

@Composable
fun MainScreenContainer(onLogout: () -> Unit) {
    val innerNavController = rememberNavController()

    // Frigo pré-sélectionné quand on arrive au scan depuis le "+" d'un frigo.
    // Vide = pas de pré-sélection (on prend le dernier modifié).
    // Remis à "" dès que l'utilisateur ouvre le scan via la barre de navigation.
    var defaultFridgeIdForScan by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController  = innerNavController,
                onScanTabClick = { defaultFridgeIdForScan = "" }
            )
        }
    ) { padding ->
        NavHost(
            navController    = innerNavController,
            startDestination = BottomNavItem.Fridges.route,
            modifier         = Modifier.padding(padding)
        ) {

            // ── Liste des frigos ──────────────────────────────────────────────
            composable(BottomNavItem.Fridges.route) {
                // FIX 3 : rechargement automatique quand l'écran revient au premier plan
                // (ex: après création d'un frigo ou retour depuis le détail)
                val lifecycleOwner = LocalLifecycleOwner.current
                val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

                FridgeListScreen(
                    onFridgeClick    = { fridgeId -> innerNavController.navigate("fridge_detail/$fridgeId") },
                    onAddFridgeClick = { innerNavController.navigate("create_fridge") },
                    // On passe l'état du cycle de vie pour que FridgeListScreen
                    // puisse déclencher un reload quand il redevient RESUMED
                    lifecycleState   = lifecycleState
                )
            }

            // ── Création d'un frigo ───────────────────────────────────────────
            composable("create_fridge") {
                FridgeCreateScreen(
                    onBackClick = { innerNavController.popBackStack() },
                    onSuccess   = { innerNavController.popBackStack() }
                )
            }

            // ── Détail d'un frigo ─────────────────────────────────────────────
            composable("fridge_detail/{fridgeId}") { backStackEntry ->
                val fridgeId = backStackEntry.arguments?.getString("fridgeId") ?: ""
                FridgeScreen(
                    fridgeId           = fridgeId,
                    onBackClick        = { innerNavController.popBackStack() },
                    onSettingsClick    = { innerNavController.navigate("fridge_settings/$fridgeId") },
                    onAddProductClick  = {
                        // Mémorise le frigo dans l'état partagé puis navigue vers l'onglet scan.
                        defaultFridgeIdForScan = fridgeId
                        innerNavController.navigate(BottomNavItem.Scan.route) {
                            popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState    = false
                        }
                    },
                    onEditProductClick = { item ->
                        val route = "edit_product/$fridgeId/${item.id}" +
                            "?name=${item.name.encodeArg()}" +
                            "&brand=${(item.brand ?: "").encodeArg()}" +
                            "&qty=${item.quantity}" +
                            "&productId=${item.productId.encodeArg()}" +
                            "&imageUrl=${(item.imageUrl ?: "").encodeArg()}" +
                            "&expiryDate=${item.expiryDate.encodeArg()}"
                        innerNavController.navigate(route)
                    }
                )
            }

            // ── Édition d'un produit ──────────────────────────────────────────
            composable(
                route     = "edit_product/{fridgeId}/{itemId}?name={name}&brand={brand}&qty={qty}&productId={productId}&imageUrl={imageUrl}&expiryDate={expiryDate}",
                arguments = listOf(
                    navArgument("fridgeId")   { type = NavType.StringType },
                    navArgument("itemId")     { type = NavType.StringType },
                    navArgument("name")       { type = NavType.StringType; defaultValue = "" },
                    navArgument("brand")      { type = NavType.StringType; defaultValue = "" },
                    navArgument("qty")        { type = NavType.StringType; defaultValue = "1" },
                    navArgument("productId")  { type = NavType.StringType; defaultValue = "" },
                    navArgument("imageUrl")   { type = NavType.StringType; defaultValue = "" },
                    navArgument("expiryDate") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val fridgeId       = backStackEntry.arguments?.getString("fridgeId")   ?: ""
                val itemId         = backStackEntry.arguments?.getString("itemId")     ?: ""
                val itemName       = backStackEntry.arguments?.getString("name")?.decodeArg() ?: ""
                val itemBrand      = backStackEntry.arguments?.getString("brand")?.decodeArg()?.takeIf { it.isNotBlank() }
                val initialQty     = backStackEntry.arguments?.getString("qty")?.toIntOrNull() ?: 1
                val productId      = backStackEntry.arguments?.getString("productId")?.decodeArg() ?: ""
                val itemImageUrl   = backStackEntry.arguments?.getString("imageUrl")?.decodeArg()?.takeIf { it.isNotBlank() }
                val initialExpiry  = backStackEntry.arguments?.getString("expiryDate")?.decodeArg() ?: ""
                EditProductScreen(
                    fridgeId       = fridgeId,
                    itemId         = itemId,
                    productId      = productId,
                    itemName       = itemName,
                    itemBrand      = itemBrand,
                    itemImageUrl   = itemImageUrl,
                    initialExpiry  = initialExpiry,
                    initialQty     = initialQty,
                    onBackClick  = { innerNavController.popBackStack() },
                    onSaved      = { innerNavController.popBackStack() },
                    onDeleted    = {
                        innerNavController.navigate("fridge_detail/$fridgeId") {
                            popUpTo("fridge_detail/$fridgeId") { inclusive = true }
                        }
                    }
                )
            }

            // ── Paramètres d'un frigo ─────────────────────────────────────────
            composable("fridge_settings/{fridgeId}") { backStackEntry ->
                val fridgeId = backStackEntry.arguments?.getString("fridgeId") ?: ""
                val context  = LocalContext.current
                val currentUserId by produceState("") {
                    value = (context.applicationContext as FrigApplication).tokenRepository.getUserId() ?: ""
                }
                FridgeSettingsScreen(
                    fridgeId        = fridgeId,
                    currentUserId   = currentUserId,
                    onMembersClick  = { innerNavController.navigate("fridge_members/$fridgeId") },
                    onBackClick     = { innerNavController.popBackStack() },
                    onFridgeDeleted = {
                        innerNavController.navigate(BottomNavItem.Fridges.route) {
                            popUpTo(BottomNavItem.Fridges.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Membres d'un frigo ────────────────────────────────────────────
            composable("fridge_members/{fridgeId}") { backStackEntry ->
                val fridgeId = backStackEntry.arguments?.getString("fridgeId") ?: ""
                val context = LocalContext.current
                val currentUserId by produceState("") {
                    value = (context.applicationContext as FrigApplication).tokenRepository.getUserId() ?: ""
                }
                FridgeMembersScreen(
                    fridgeId      = fridgeId,
                    currentUserId = currentUserId,
                    onBackClick   = { innerNavController.popBackStack() }
                )
            }

            // ── Scanner ───────────────────────────────────────────────────────
            // defaultFridgeIdForScan est un état partagé du MainScreenContainer.
            // Il est valorisé quand on vient du "+" d'un frigo, vide sinon.
            composable(BottomNavItem.Scan.route) {
                ScanScreen(
                    onManualEntryClick = {
                        val route = "add_product_form" +
                            "?defaultFridgeId=${defaultFridgeIdForScan.encodeArg()}"
                        innerNavController.navigate(route)
                    },
                    onProductScanned   = { product ->
                        val route = "add_product_form" +
                            "?barcode=${product.barcode.encodeArg()}" +
                            "&name=${product.name.encodeArg()}" +
                            "&brand=${product.brand.encodeArg()}" +
                            "&imageUrl=${product.imageUrl.encodeArg()}" +
                            "&productId=${product.productId.encodeArg()}" +
                            "&defaultFridgeId=${defaultFridgeIdForScan.encodeArg()}"
                        innerNavController.navigate(route)
                    },
                    onBarcodeNotFound  = { barcode ->
                        val route = "add_product_form" +
                            "?barcode=${barcode.encodeArg()}" +
                            "&defaultFridgeId=${defaultFridgeIdForScan.encodeArg()}"
                        innerNavController.navigate(route)
                    }
                )
            }

            // ── Formulaire ajout produit ───────────────────────────────────────
            // Reçoit les données optionnelles du scan + le frigo pré-sélectionné via query params
            composable(
                route     = "add_product_form?barcode={barcode}&name={name}&brand={brand}&imageUrl={imageUrl}&productId={productId}&defaultFridgeId={defaultFridgeId}",
                arguments = listOf(
                    navArgument("barcode")         { type = NavType.StringType; defaultValue = "" },
                    navArgument("name")            { type = NavType.StringType; defaultValue = "" },
                    navArgument("brand")           { type = NavType.StringType; defaultValue = "" },
                    navArgument("imageUrl")        { type = NavType.StringType; defaultValue = "" },
                    navArgument("productId")       { type = NavType.StringType; defaultValue = "" },
                    navArgument("defaultFridgeId") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val barcode         = backStackEntry.arguments?.getString("barcode")?.decodeArg()
                val name            = backStackEntry.arguments?.getString("name")?.decodeArg()
                val brand           = backStackEntry.arguments?.getString("brand")?.decodeArg()
                val imageUrl        = backStackEntry.arguments?.getString("imageUrl")?.decodeArg()
                val productId       = backStackEntry.arguments?.getString("productId")?.decodeArg()
                val defaultFridgeId = backStackEntry.arguments?.getString("defaultFridgeId")?.decodeArg()

                ProductFormScreen(
                    prefilledBarcode   = barcode?.takeIf         { it.isNotBlank() },
                    prefilledName      = name?.takeIf            { it.isNotBlank() },
                    prefilledBrand     = brand?.takeIf           { it.isNotBlank() },
                    prefilledImageUrl  = imageUrl?.takeIf        { it.isNotBlank() },
                    prefilledProductId = productId?.takeIf       { it.isNotBlank() },
                    prefilledFridgeId  = defaultFridgeId?.takeIf { it.isNotBlank() },
                    onBackClick        = { innerNavController.popBackStack() },
                    onAddSuccess       = {
                        innerNavController.navigate(BottomNavItem.Fridges.route) {
                            popUpTo(BottomNavItem.Fridges.route) { inclusive = false }
                        }
                    }
                )
            }

            // ── Profil ────────────────────────────────────────────────────────
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

// ─── Barre de navigation inférieure ──────────────────────────────────────────

@Composable
fun BottomNavigationBar(navController: NavHostController, onScanTabClick: () -> Unit = {}) {
    val items = listOf(
        BottomNavItem.Fridges,
        BottomNavItem.Scan,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier       = Modifier.height(80.dp)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
                || (item == BottomNavItem.Fridges && (
                    currentRoute?.startsWith("fridge_") == true ||
                    currentRoute?.startsWith("create_fridge") == true
                ))

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = item.label,
                        modifier           = Modifier.size(26.dp)
                    )
                },
                label = {
                    Text(
                        text       = item.label,
                        fontSize   = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color(0xFF2EAA84),
                    selectedTextColor   = Color(0xFF2EAA84),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor      = Color(0xFFE6F4EF)
                ),
                onClick = {
                    val alreadyInSection  = isSelected && currentRoute != item.route
                    val isScanTab         = item == BottomNavItem.Scan
                    // On est actuellement dans la zone scan (écran scan ou formulaire d'ajout)
                    val currentlyInScan   = currentRoute == BottomNavItem.Scan.route
                            || currentRoute?.startsWith("add_product_form") == true

                    if (isScanTab) onScanTabClick()

                    // La zone scan ne sauvegarde/restaure jamais l'état :
                    //  - quand on ARRIVE sur scan (isScanTab)
                    //  - quand on PART de scan vers un autre onglet (currentlyInScan)
                    // Sans ça, launchSingleTop empêche le recompose quand fridge_list
                    // est déjà au sommet de la pile après le popUpTo.
                    val useSaveRestore = !isScanTab && !currentlyInScan && !alreadyInSection
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = useSaveRestore
                        }
                        launchSingleTop = true
                        restoreState    = useSaveRestore
                    }
                }
            )
        }
    }
}
