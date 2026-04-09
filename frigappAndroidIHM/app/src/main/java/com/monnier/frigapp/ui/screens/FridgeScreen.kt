package com.monnier.frigapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.monnier.frigapp.ui.viewmodels.FridgeItemDisplay
import com.monnier.frigapp.ui.viewmodels.FridgeViewModel

@Composable
fun FridgeScreen(
    fridgeId: String, // On a besoin de l'ID pour savoir quel frigo charger
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onEditProductClick: (FridgeItemDisplay) -> Unit,
    viewModel: FridgeViewModel = viewModel()
) {
    // Charger les données à l'ouverture
    LaunchedEffect(fridgeId) {
        viewModel.fetchFridgeData(fridgeId)
    }

    val products  by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // ─── Recherche ────────────────────────────────────────────────────────────
    var searchQuery    by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    // Ouvre le clavier automatiquement quand la barre de recherche apparaît
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) searchFocusRequester.requestFocus()
    }

    // Réinitialise l'item développé à chaque changement de query
    var expandedProductId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(searchQuery) { expandedProductId = null }

    // Filtre en temps réel (nom ou marque, insensible à la casse)
    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter { p ->
            p.name.contains(searchQuery, ignoreCase = true) ||
            p.brand?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    val priorityProducts = filteredProducts.filter { it.isUrgent }
    val otherProducts    = filteredProducts.filter { !it.isUrgent }

    var productToDelete by remember { mutableStateOf<FridgeItemDisplay?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {

            // --- 1. EN-TÊTE VERT (Dynamique) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .background(Color(0xFF2EAA84))
                    .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ChevronLeft, "Retour", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Text(viewModel.fridgeName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Row {
                            // Loupe / fermeture recherche
                            IconButton(onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) searchQuery = ""
                            }) {
                                Icon(
                                    if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = if (isSearchActive) "Fermer la recherche" else "Rechercher",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = onSettingsClick) {
                                Icon(Icons.Default.Settings, "Paramètres", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.padding(start = 12.dp)) {
                        Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                            Text("${products.size} produits", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 12.sp)
                        }
                        if (priorityProducts.isNotEmpty() && searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = Color(0xFFD03D2F), shape = RoundedCornerShape(12.dp)) {
                                Text("${priorityProducts.size} expirent bientôt", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    // ── Barre de recherche (animée) ───────────────────────────
                    AnimatedVisibility(
                        visible = isSearchActive,
                        enter   = expandVertically(),
                        exit    = shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value         = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder   = { Text("Rechercher un produit…", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp) },
                            singleLine    = true,
                            leadingIcon   = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.8f)) },
                            trailingIcon  = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, "Effacer", tint = Color.White)
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            shape  = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor      = Color.White,
                                unfocusedTextColor    = Color.White,
                                cursorColor           = Color.White,
                                focusedBorderColor    = Color.White.copy(alpha = 0.6f),
                                unfocusedBorderColor  = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .focusRequester(searchFocusRequester)
                        )
                    }
                }
            }

            // --- 2. LISTE DES PRODUITS (Dynamique) ---
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2EAA84))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Message aucun résultat de recherche
                    if (searchQuery.isNotBlank() && filteredProducts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint     = Color.LightGray,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Aucun produit pour « $searchQuery »",
                                        color    = Color.Gray,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // SECTION PRIORITÉ
                    if (priorityProducts.isNotEmpty()) {
                        item { SectionHeader(Icons.Default.WarningAmber, "À CONSOMMER EN PRIORITÉ", Color(0xFFD03D2F)) }
                        item {
                            ProductContainer {
                                priorityProducts.forEachIndexed { index, product ->
                                    ExpandableProductRow(
                                        name = product.name,
                                        subtitle = if(product.daysRemaining < 0) "Expiré !" else "Dans ${product.daysRemaining} jours",
                                        date = "×${product.quantity}",
                                        imageUrl = product.imageUrl,
                                        statusColor = Color(0xFFD03D2F),
                                        isExpanded = expandedProductId == product.id,
                                        onClick = { expandedProductId = if (expandedProductId == product.id) null else product.id },
                                        onEdit = { onEditProductClick(product) },
                                        onDelete = { productToDelete = product }
                                    )
                                    if (index < priorityProducts.size - 1) HorizontalDivider(color = Color(0xFFF1F3F5))
                                }
                            }
                        }
                    }

                    // SECTION AUTRES
                    if (otherProducts.isNotEmpty()) {
                        item { SectionHeader(null, "AUTRES PRODUITS", Color.Gray) }
                        item {
                            ProductContainer {
                                otherProducts.forEachIndexed { index, product ->
                                    ExpandableProductRow(
                                        name = product.name,
                                        subtitle = "×${product.quantity}${if (product.brand != null) " · ${product.brand}" else ""}",
                                        date = "${product.daysRemaining}j",
                                        imageUrl = product.imageUrl,
                                        statusColor = Color(0xFF2EAA84),
                                        isExpanded = expandedProductId == product.id,
                                        onClick = { expandedProductId = if (expandedProductId == product.id) null else product.id },
                                        onEdit = { onEditProductClick(product) },
                                        onDelete = { productToDelete = product }
                                    )
                                    if (index < otherProducts.size - 1) HorizontalDivider(color = Color(0xFFF1F3F5))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pop-up de suppression (corrigée pour utiliser l'objet complet)
        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Supprimer ${productToDelete?.name} ?") },
                text = { Text("Voulez-vous vraiment retirer ce produit de votre frigo ?") },
                confirmButton = {
                    TextButton(onClick = {
                        productToDelete?.let { viewModel.deleteProduct(it.id, it.productId) }
                        productToDelete = null
                    }) {
                        Text("Supprimer", color = Color(0xFFD03D2F), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Annuler", color = Color.Gray)
                    }
                }
            )
        }

        // FAB Scanner
        FloatingActionButton(
            onClick = onAddProductClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Color(0xFF2EAA84),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, "Scanner")
        }
    }
}

// --- COMPOSANTS INTERNES ---

@Composable
fun ExpandableProductRow(
    name: String,
    subtitle: String,
    date: String,
    imageUrl: String?,
    statusColor: Color,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize() // Animation fluide de l'ouverture
            .then(
                if (isExpanded) Modifier.border(2.dp, Color(0xFF2EAA84), RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable { onClick() }
    ) {
        // Ligne de produit classique
        ProductRow(name, subtitle, date, imageUrl, statusColor)

        // Boutons d'actions (visibles uniquement si étendu)
        if (isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bouton Modifier
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EAA84)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Modifier", fontSize = 14.sp)
                }

                // Bouton Supprimer
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F1)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD03D2F).copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFD03D2F), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Supprimer", color = Color(0xFFD03D2F), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ProductContainer(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.5.dp),
        content = content
    )
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector?, title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
        if (icon != null) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun ProductRow(name: String, subtitle: String, date: String, imageUrl: String?, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8F9FA)),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.Kitchen, null, tint = Color.DarkGray, modifier = Modifier.size(22.dp))
            }
        }

        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = statusColor, fontSize = 12.sp)
        }

        Surface(
            color = statusColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}