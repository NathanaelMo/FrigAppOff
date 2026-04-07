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
    currentUserId: String,
    onMembersClick: () -> Unit,
    onBackClick: () -> Unit,
    onFridgeDeleted: () -> Unit,
    viewModel: FridgeSettingsViewModel = viewModel()
) {
    LaunchedEffect(fridgeId) {
        viewModel.loadFridge(fridgeId)
    }

    val members           by viewModel.members.collectAsState()
    val showConfirmDialog by viewModel.showConfirmDialog.collectAsState()

    // Même logique que FridgeMembersScreen : on croise la liste des membres
    // avec le currentUserId pour obtenir le vrai rôle de l'utilisateur connecté.
    val isOwner = members.any { it.userId?.toString() == currentUserId && it.role?.value == "owner" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        // ── EN-TÊTE ───────────────────────────────────────────────────────────
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            viewModel.fridgeName,
                            color    = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Badge rôle — couleur différente selon propriétaire / collaborateur
                        Surface(
                            color = if (isOwner) Color(0xFFFFD700).copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOwner) Icons.Default.Star else Icons.Default.Person,
                                    contentDescription = null,
                                    tint     = if (isOwner) Color(0xFFFFD700) else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (isOwner) "Propriétaire" else "Collaborateur",
                                    color    = if (isOwner) Color(0xFFFFD700) else Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
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
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── SECTION NOM DU FRIGO ─────────────────────────────────────────
            if (isOwner) {
                // ─── Vue PROPRIÉTAIRE : champ éditable ───────────────────────
                SettingsCard {
                    Text(
                        "NOM DU FRIGO",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = viewModel.fridgeName,
                        onValueChange = { viewModel.fridgeName = it },
                        enabled       = !viewModel.isLoading,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Color(0xFF2EAA84),
                            focusedTextColor     = Color(0xFF1C1B1F),
                            unfocusedTextColor   = Color(0xFF1C1B1F)
                        )
                    )
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
                                color       = Color.White,
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enregistrer le nom")
                        }
                    }
                }
            } else {
                // ─── Vue COLLABORATEUR : affichage en lecture seule ───────────
                SettingsCard {
                    Row(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment   = Alignment.CenterVertically
                    ) {
                        Text(
                            "NOM DU FRIGO",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.Gray
                        )
                        Surface(
                            color = Color(0xFFE9ECEF),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    null,
                                    tint     = Color.Gray,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Propriétaire uniquement",
                                    fontSize = 10.sp,
                                    color    = Color.Gray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Kitchen,
                            null,
                            tint     = Color(0xFF2EAA84),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            viewModel.fridgeName,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            color      = Color(0xFF1C1B1F)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Bandeau informatif
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint     = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Seul le propriétaire peut modifier le nom du frigo.",
                            fontSize = 12.sp,
                            color    = Color(0xFF92400E)
                        )
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

            // ── ZONE DANGEREUSE (owner) / QUITTER (collaborateur) ────────────
            if (isOwner) {
                // ─── Vue PROPRIÉTAIRE : suppression ──────────────────────────
                val dangerColor = Color(0xFFD03D2F)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dangerColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .background(dangerColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                null,
                                tint     = dangerColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "ZONE DANGEREUSE",
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color      = dangerColor
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsCard(onClick = { viewModel.showConfirmDialog() }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Supprimer ce frigo",
                                        color      = dangerColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Supprime définitivement pour tous les membres",
                                        color    = dangerColor.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                                Icon(Icons.Default.DeleteForever, null, tint = dangerColor)
                            }
                        }
                    }
                }
            } else {
                // ─── Vue COLLABORATEUR : quitter le frigo ─────────────────────
                val leaveColor = Color(0xFF6B7280)
                SettingsCard(onClick = { viewModel.showConfirmDialog() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier          = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF3F4F6), CircleShape),
                            contentAlignment  = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                null,
                                tint     = leaveColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Quitter ce frigo",
                                color      = leaveColor,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            )
                            Text(
                                "Tu seras retiré de la liste des membres",
                                color    = leaveColor.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                    }
                }
            }
        }
    }

    // ── DIALOG DE CONFIRMATION ───────────────────────────────────────────────
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            icon = {
                Icon(
                    if (isOwner) Icons.Default.DeleteForever else Icons.Default.ExitToApp,
                    null,
                    tint = if (isOwner) Color(0xFFD03D2F) else Color(0xFF6B7280)
                )
            },
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
                        if (isOwner) viewModel.deleteFridge(onSuccess = onFridgeDeleted)
                        else         viewModel.leaveFridge(onSuccess = onFridgeDeleted)
                    }
                ) {
                    Text(
                        if (isOwner) "Supprimer" else "Quitter",
                        color      = if (isOwner) Color(0xFFD03D2F) else Color(0xFF6B7280),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDialog() }) {
                    Text("Annuler", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape          = RoundedCornerShape(20.dp)
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
