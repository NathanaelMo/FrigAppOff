package com.monnier.frigapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.monnier.frigapp.ui.viewmodels.EditProductState
import com.monnier.frigapp.ui.viewmodels.EditProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    fridgeId:       String,
    itemId:         String,
    productId:      String  = "",
    itemName:       String  = "",
    itemBrand:      String? = null,
    itemImageUrl:   String? = null,
    initialExpiry:  String  = "",
    initialQty:     Int     = 1,
    onBackClick:    () -> Unit,
    onSaved:        () -> Unit,
    onDeleted:      () -> Unit,
    viewModel: EditProductViewModel = viewModel()
) {
    LaunchedEffect(fridgeId, itemId) {
        viewModel.load(fridgeId, itemId, productId)
    }

    var quantity       by remember(initialQty)    { mutableIntStateOf(initialQty) }
    var expirationDate by remember(initialExpiry) { mutableStateOf(initialExpiry) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    // Navigation après action
    LaunchedEffect(state) {
        when (state) {
            is EditProductState.Saved   -> { viewModel.resetState(); onSaved() }
            is EditProductState.Deleted -> { viewModel.resetState(); onDeleted() }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {

            // ── EN-TÊTE ───────────────────────────────────────────────────────
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
                        Text("Modifier le produit", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Kitchen, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(itemName.ifBlank { "Produit" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (!itemBrand.isNullOrBlank())
                                    Text(itemBrand, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
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

                // ── Image du produit ──────────────────────────────────────────
                if (!itemImageUrl.isNullOrBlank()) {
                    Card(
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model            = itemImageUrl,
                            contentDescription = itemName,
                            contentScale     = ContentScale.Fit,
                            modifier         = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }

                // ── Date de péremption ────────────────────────────────────────
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
                                focusedBorderColor   = Color(0xFF2EAA84),
                                focusedTextColor     = Color(0xFF1C1B1F),
                                unfocusedTextColor   = Color(0xFF1C1B1F)
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

                // ── Quantité ──────────────────────────────────────────────────
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

                // ── Erreur ────────────────────────────────────────────────────
                if (state is EditProductState.Error) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F1)),
                        shape  = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFD03D2F), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text((state as EditProductState.Error).message, color = Color(0xFFD03D2F), fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Actions ───────────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick  = { viewModel.saveChanges(quantity, expirationDate) },
                        enabled  = state !is EditProductState.Loading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EAA84))
                    ) {
                        if (state is EditProductState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Enregistrer les modifications", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(
                        onClick  = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD03D2F))
                    ) {
                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Supprimer définitivement", fontWeight = FontWeight.Bold)
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

        // ── Dialog de confirmation ─────────────────────────────────────────────
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title  = { Text("Supprimer le produit ?") },
                text   = { Text("Cette action est irréversible.") },
                confirmButton = {
                    TextButton(onClick = { showDeleteDialog = false; viewModel.deleteItem() }) {
                        Text("Supprimer", color = Color(0xFFD03D2F), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Annuler", color = Color.Gray)
                    }
                },
                shape          = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}
