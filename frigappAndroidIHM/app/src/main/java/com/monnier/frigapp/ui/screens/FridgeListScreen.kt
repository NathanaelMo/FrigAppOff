package com.monnier.frigapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monnier.frigapp.ui.viewmodels.FridgeListViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FridgeListScreen(
    onFridgeClick: (String) -> Unit,
    onAddFridgeClick: () -> Unit,
    // FIX 3 : reçu depuis AppNavigation pour déclencher un reload au RESUMED
    lifecycleState: Lifecycle.State = Lifecycle.State.RESUMED,
    viewModel: FridgeListViewModel = viewModel()
) {
    val fridges      by viewModel.fridges.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // FIX 3 : recharge la liste chaque fois que l'écran redevient actif
    // (retour depuis création d'un frigo, depuis le détail, etc.)
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            viewModel.loadFridges()
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh  = { viewModel.loadFridges() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // ── EN-TÊTE VERT ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(Color(0xFF2EAA84))
                .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Text(
                    text       = "Mes Frigos",
                    color      = Color.White,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = "Gérez vos différents espaces de stockage",
                    color    = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // FIX 5 : bandeau d'erreur visible sous l'en-tête
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F1)),
                shape  = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier          = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint     = Color(0xFFD03D2F),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text     = errorMessage ?: "",
                        color    = Color(0xFFD03D2F),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick  = { viewModel.loadFridges() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Réessayer",
                            tint     = Color(0xFFD03D2F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ── LISTE + PULL-TO-REFRESH ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            if (fridges.isEmpty() && !isLoading && errorMessage == null) {
                // État vide
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Kitchen,
                        null,
                        modifier = Modifier.size(100.dp),
                        tint     = Color.LightGray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Aucun frigo trouvé", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Text(
                        "Tirez vers le bas pour rafraîchir\nou créez-en un avec le bouton +",
                        color    = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(fridges) { fridge ->
                        FridgeCard(
                            name        = fridge.name ?: "Sans nom",
                            role        = fridge.role?.value ?: "collaborator",
                            memberCount = fridge.memberCount ?: 0,
                            onClick     = { onFridgeClick(fridge.id?.toString() ?: "") }
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing   = isLoading,
                state        = pullRefreshState,
                modifier     = Modifier.align(Alignment.TopCenter),
                contentColor = Color(0xFF2EAA84),
                backgroundColor = Color.White
            )

            FloatingActionButton(
                onClick        = onAddFridgeClick,
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = Color(0xFF2EAA84),
                contentColor   = Color.White,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un frigo")
            }
        }
    }
}

// ─── Composants internes ──────────────────────────────────────────────────────

@Composable
fun FridgeCard(
    name: String,
    role: String,
    memberCount: Int,
    onClick: () -> Unit
) {
    val isOwner = role == "owner"
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier         = Modifier.size(48.dp).background(Color(0xFFE6F4EF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Kitchen, null, tint = Color(0xFF2EAA84), modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Groups, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(
                        " $memberCount membre${if (memberCount > 1) "s" else ""}",
                        color = Color.Gray, fontSize = 12.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = if (isOwner) Color(0xFFE6F4EF) else Color(0xFFEFF3FB),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            if (isOwner) "Proprio" else "Membre",
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize   = 10.sp,
                            color      = if (isOwner) Color(0xFF2EAA84) else Color(0xFF1B63D1),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
