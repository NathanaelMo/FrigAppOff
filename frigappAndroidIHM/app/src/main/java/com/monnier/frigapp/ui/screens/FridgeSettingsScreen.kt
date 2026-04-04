package com.monnier.frigapp.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monnier.frigapp.ui.viewmodels.FridgeSettingsViewModel

@Composable
fun FridgeSettingsScreen(
    fridgeId: String,
    onMembersClick: () -> Unit,
    onBackClick: () -> Unit,
    onFridgeDeleted: () -> Unit,        // Navigation vers la liste après suppression/départ
    viewModel: FridgeSettingsViewModel = viewModel()
) {
    // Chargement des données au démarrage
    LaunchedEffect(fridgeId) {
        viewModel.loadFridge(fridgeId)
    }

    val isOwner            = viewModel.userRole == "owner"
    val showConfirmDialog  by viewModel.showConfirmDialog.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
    ) {
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
                Column {
                    Text(
                        "Paramètres du frigo",
                        color      = Color.White,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text      = "${viewModel.fridgeName} · ${if (isOwner) "Propriétaire" else "Collaborateur"}",
                        color     = Color.White.copy(alpha = 0.8f),
                        fontSize  = 14.sp
                    )
                }
            }
        }

        // Erreur globale
        if (viewModel.errorMessage != null) {
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F1)),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text     = viewModel.errorMessage ?: "",
                    modifier = Modifier.padding(12.dp),
                    color    = Color(0xFFD03D2F),
                    fontSize = 13.sp
                )
            }
        }

        Column(
            modifier  = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── SECTION NOM DU FRIGO ─────────────────────────────────────────
            SettingsCard {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "NOM DU FRIGO",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.Gray
                    )
                    if (!isOwner) {
                        Surface(
                            color = Color(0xFFE9ECEF),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Proprio uniquement",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color    = Color.Gray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value         = viewModel.fridgeName,
                    onValueChange = { if (isOwner) viewModel.fridgeName = it },
                    enabled       = isOwner && !viewModel.isLoading,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color(0xFFF1F3F5),
                        disabledBorderColor    = Color(0xFFDEE2E6)
                    )
                )
                if (isOwner) {
                    Button(
                        onClick  = { viewModel.renameFridge(onSuccess = {}) },
                        enabled  = !viewModel.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EAA84)),
                        shape  = RoundedCornerShape(12.dp)
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color    = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Enregistrer le nom")
                        }
                    }
                }
            }

            // ── SECTION MEMBRES ──────────────────────────────────────────────
            SettingsCard(onClick = onMembersClick) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Membres du frigo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Gérer les accès", color = Color.Gray, fontSize = 13.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                }
            }

            // ── ZONE DANGEREUSE / QUITTER ────────────────────────────────────
            val dangerColor = Color(0xFFD03D2F)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dangerColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .background(dangerColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        if (isOwner) "ZONE DANGEREUSE" else "QUITTER CE FRIGO",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = dangerColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsCard(onClick = { viewModel.showConfirmDialog() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (isOwner) "Supprimer ce frigo" else "Quitter ce frigo",
                                    color      = dangerColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (isOwner) "Supprime pour tous les membres" else "Tu seras retiré des membres",
                                    color    = dangerColor.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                            Icon(Icons.Default.PhonelinkErase, null, tint = dangerColor)
                        }
                    }
                }
            }
        }
    }

    // ── DIALOG DE CONFIRMATION ───────────────────────────────────────────────
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            title = {
                Text(
                    if (isOwner) "Supprimer ce frigo ?" else "Quitter ce frigo ?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (isOwner)
                        "Cette action est irréversible. Tous les produits et membres seront supprimés."
                    else
                        "Tu n'auras plus accès à ce frigo ni à ses produits."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissConfirmDialog()
                        if (isOwner) {
                            viewModel.deleteFridge(onSuccess = onFridgeDeleted)
                        } else {
                            viewModel.leaveFridge(onSuccess = onFridgeDeleted)
                        }
                    }
                ) {
                    Text(
                        if (isOwner) "Supprimer" else "Quitter",
                        color      = Color(0xFFD03D2F),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDialog() }) {
                    Text("Annuler", color = Color.Gray)
                }
            }
        )
    }
}

// ─── Composants internes ──────────────────────────────────────────────────────

@Composable
fun SettingsCard(
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = Modifier.fillMaxWidth()
    Card(
        modifier  = if (onClick != null) base.clickable(onClick = onClick) else base,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}
