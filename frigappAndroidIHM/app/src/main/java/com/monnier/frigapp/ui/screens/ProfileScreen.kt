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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monnier.frigapp.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val userName     by viewModel.userName.collectAsState()
    val userEmail    by viewModel.userEmail.collectAsState()
    val fridgeCount  by viewModel.fridgeCount.collectAsState()
    val productCount by viewModel.productCount.collectAsState()
    val memberCount  by viewModel.memberCount.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()

    // Initiale de l'avatar (première lettre du prénom)
    val initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    var alertsEnabled  by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        // ── EN-TÊTE VERT AVEC AVATAR ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(Color(0xFF2EAA84))
                .padding(top = 48.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar avec initiale
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color       = Color.White,
                            modifier    = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(initial, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text       = userName.ifBlank { "Chargement…" },
                    color      = Color.White,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = userEmail,
                    color    = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── STATISTIQUES ──────────────────────────────────────────────────
            Column {
                Text(
                    "MES STATISTIQUES",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value    = if (isLoading) "…" else fridgeCount.toString(),
                        label    = "Frigos",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = if (isLoading) "…" else productCount.toString(),
                        label    = "Produits",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value    = if (isLoading) "…" else memberCount.toString(),
                        label    = "Membres",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── PARAMÈTRES ────────────────────────────────────────────────────
            Column {
                Text(
                    "PARAMÈTRES",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.5.dp)
                ) {
                    Column {
                        SettingsToggleRow(Icons.Default.Notifications, "Alertes DLC", alertsEnabled)  { alertsEnabled  = it }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3F5))
                        SettingsToggleRow(Icons.Default.DarkMode, "Mode sombre", darkModeEnabled) { darkModeEnabled = it }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF1F3F5))
                        SettingsClickRow(Icons.Default.Language, "Langue", "Français") { /* TODO */ }
                    }
                }
            }

            // ── DÉCONNEXION ───────────────────────────────────────────────────
            Button(
                onClick  = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F1)),
                shape    = RoundedCornerShape(16.dp),
                border   = BorderStroke(1.dp, Color(0xFFD03D2F).copy(alpha = 0.1f))
            ) {
                Text("Se déconnecter", color = Color(0xFFD03D2F), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Composants internes ──────────────────────────────────────────────────────

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF2EAA84))
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Text(label, modifier = Modifier.padding(start = 12.dp).weight(1f), fontSize = 15.sp)
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2EAA84))
        )
    }
}

@Composable
fun SettingsClickRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Text(label, modifier = Modifier.padding(start = 12.dp).weight(1f), fontSize = 15.sp)
        Text(value, color = Color.Gray, fontSize = 13.sp)
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}
