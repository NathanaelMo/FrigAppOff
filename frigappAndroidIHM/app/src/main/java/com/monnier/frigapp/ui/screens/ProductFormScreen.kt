package com.monnier.frigapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.monnier.frigapp.ui.viewmodels.ProductFormState
import com.monnier.frigapp.ui.viewmodels.ProductFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    prefilledName:      String? = null,
    prefilledBrand:     String? = null,
    prefilledBarcode:   String? = null,
    prefilledImageUrl:  String? = null,
    prefilledProductId: String? = null,
    onBackClick:   () -> Unit,
    onAddSuccess:  () -> Unit,
    viewModel: ProductFormViewModel = viewModel()
) {
    val isFromScan = !prefilledBarcode.isNullOrBlank()
    val hasOffData = isFromScan && !prefilledName.isNullOrBlank()

    var productName    by remember { mutableStateOf(prefilledName ?: "") }
    var quantity       by remember { mutableIntStateOf(1) }
    var expirationDate by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val fridges    by viewModel.fridges.collectAsState()
    val formState  by viewModel.state.collectAsState()

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Frigo sélectionné (id + nom)
    var selectedFridgeId   by remember { mutableStateOf("") }
    var selectedFridgeName by remember { mutableStateOf("") }

    // Initialise la sélection dès que la liste est chargée
    LaunchedEffect(fridges) {
        if (fridges.isNotEmpty() && selectedFridgeId.isEmpty()) {
            selectedFridgeId   = fridges.first().id?.toString() ?: ""
            selectedFridgeName = fridges.first().name ?: ""
        }
    }

    // Navigation après succès
    LaunchedEffect(formState) {
        if (formState is ProductFormState.Success) {
            viewModel.resetState()
            onAddSuccess()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {

        // ── EN-TÊTE VERT ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(Color(0xFF2EAA84))
                .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ChevronLeft, "Retour", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        if (isFromScan) "Produit scanné" else "Ajout manuel",
                        color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    if (hasOffData || isFromScan) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            if (!prefilledImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = prefilledImageUrl, contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.Kitchen, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (hasOffData) (prefilledName ?: "") else "Produit inconnu",
                                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
                                )
                                if (!prefilledBrand.isNullOrBlank())
                                    Text(prefilledBrand, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                        Surface(
                            modifier = Modifier.padding(top = 8.dp),
                            color    = if (hasOffData) Color.White.copy(alpha = 0.15f) else Color(0xFFD03D2F).copy(alpha = 0.3f),
                            shape    = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (hasOffData) "Trouvé sur Open Food Facts ✓" else "Produit non référencé · Saisie manuelle",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White, fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Nom du produit ────────────────────────────────────────────────
            FormCard {
                Text("Nom du produit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = productName, onValueChange = { productName = it },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Ex: Lait entier, Yaourt…") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2EAA84),
                        focusedTextColor = Color(0xFF1C1B1F),
                        unfocusedTextColor = Color(0xFF1C1B1F)
                    )
                )
                if (!prefilledBarcode.isNullOrBlank())
                    Text("EAN : $prefilledBarcode", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }

            // ── Date de péremption ────────────────────────────────────────────
            FormCard {
                Text("Date limite de consommation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = expirationDate,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Sélectionner une date") },
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Calendrier")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2EAA84),
                            focusedTextColor   = Color(0xFF1C1B1F),
                            unfocusedTextColor = Color(0xFF1C1B1F)
                        )
                    )
                    // Overlay transparent qui rend tout le champ cliquable
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showDatePicker = true }
                    )
                }
            }

            // ── Quantité ──────────────────────────────────────────────────────
            FormCard {
                Text("Quantité", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick  = { if (quantity > 1) quantity-- },
                        modifier = Modifier.background(Color(0xFFF1F3F5), RoundedCornerShape(12.dp))
                    ) { Icon(Icons.Default.Remove, null) }
                    Text(quantity.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick  = { quantity++ },
                        modifier = Modifier.background(Color(0xFFE6F4EF), RoundedCornerShape(12.dp))
                    ) { Icon(Icons.Default.Add, null, tint = Color(0xFF2EAA84)) }
                }
            }

            // ── Choix du frigo ────────────────────────────────────────────────
            FormCard {
                Text("Ajouter au frigo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                if (fridges.isEmpty()) {
                    Text("Chargement des frigos…", fontSize = 13.sp, color = Color.Gray)
                } else {
                    ExposedDropdownMenuBox(expanded = isMenuExpanded, onExpandedChange = { isMenuExpanded = it }) {
                        OutlinedTextField(
                            value = selectedFridgeName, onValueChange = {},
                            readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Kitchen, null, tint = Color.Gray) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1C1B1F),
                                unfocusedTextColor = Color(0xFF1C1B1F)
                            )
                        )
                        ExposedDropdownMenu(expanded = isMenuExpanded, onDismissRequest = { isMenuExpanded = false }) {
                            fridges.forEach { fridge ->
                                DropdownMenuItem(
                                    text = { Text(fridge.name ?: "") },
                                    onClick = {
                                        selectedFridgeId   = fridge.id?.toString() ?: ""
                                        selectedFridgeName = fridge.name ?: ""
                                        isMenuExpanded     = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Message d'erreur ──────────────────────────────────────────────
            if (formState is ProductFormState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F1)),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFD03D2F), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text((formState as ProductFormState.Error).message, color = Color(0xFFD03D2F), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Bouton valider ────────────────────────────────────────────────
            Button(
                onClick  = {
                    viewModel.addItem(
                        productId     = prefilledProductId ?: "",
                        fridgeId      = selectedFridgeId,
                        quantity      = quantity,
                        expiryDateStr = expirationDate,
                        barcode       = prefilledBarcode,
                        name          = productName.takeIf { it.isNotBlank() }
                    )
                },
                enabled  = formState !is ProductFormState.Loading && selectedFridgeId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EAA84))
            ) {
                if (formState is ProductFormState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Ajouter au frigo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convertissez les millis en format "JJ/MM/AAAA"
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            expirationDate = date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

