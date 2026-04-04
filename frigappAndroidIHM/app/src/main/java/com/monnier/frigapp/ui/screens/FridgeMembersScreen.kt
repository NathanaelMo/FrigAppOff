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
import com.monnier.frigapp.generate.model.MemberSummary
import com.monnier.frigapp.ui.viewmodels.FridgeMembersViewModel

@Composable
fun FridgeMembersScreen(
    fridgeId:    String,
    currentUserId: String,   // pour marquer "Moi" et détecter le rôle
    onBackClick: () -> Unit,
    viewModel: FridgeMembersViewModel = viewModel()
) {
    LaunchedEffect(fridgeId) { viewModel.loadMembers(fridgeId) }

    val members      by viewModel.members.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val inviteError  by viewModel.inviteError.collectAsState()
    val inviteSuccess by viewModel.inviteSuccess.collectAsState()

    var inviteEmail by remember { mutableStateOf("") }

    // Vide le champ après une invitation réussie
    LaunchedEffect(inviteSuccess) {
        if (inviteSuccess) { inviteEmail = ""; viewModel.resetInviteSuccess() }
    }

    val isOwner       = members.any { it.userId?.toString() == currentUserId && it.role?.value == "owner" }
    val owners        = members.filter { it.role?.value == "owner" }
    val collaborators = members.filter { it.role?.value == "collaborator" }

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
                    Text("Membres", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("${members.size} membre${if (members.size > 1) "s" else ""}",
                        color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // ── Chargement / Erreur ───────────────────────────────────────────
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2EAA84))
                }
            } else if (errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F1)), shape = RoundedCornerShape(12.dp)) {
                    Text(errorMessage ?: "", modifier = Modifier.padding(12.dp), color = Color(0xFFD03D2F))
                }
            } else {
                // ── Propriétaire ──────────────────────────────────────────────
                if (owners.isNotEmpty()) {
                    Text("PROPRIÉTAIRE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    MemberCard {
                        owners.forEach { member ->
                            MemberRow(
                                member      = member,
                                isMe        = member.userId?.toString() == currentUserId,
                                showDelete  = false,
                                onDelete    = {}
                            )
                        }
                    }
                }

                // ── Collaborateurs ────────────────────────────────────────────
                if (collaborators.isNotEmpty()) {
                    Text("COLLABORATEURS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    MemberCard {
                        collaborators.forEachIndexed { index, member ->
                            MemberRow(
                                member     = member,
                                isMe       = member.userId?.toString() == currentUserId,
                                showDelete = isOwner,
                                onDelete   = { viewModel.removeMember(member.userId?.toString() ?: "") }
                            )
                            if (index < collaborators.size - 1)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F3F5))
                        }
                    }
                }

                // ── Inviter (owner uniquement) ────────────────────────────────
                if (isOwner) {
                    MemberCard {
                        Text("Inviter par email", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value         = inviteEmail,
                                onValueChange = { inviteEmail = it },
                                placeholder   = { Text("ami@email.com", fontSize = 14.sp) },
                                modifier      = Modifier.weight(1f),
                                shape         = RoundedCornerShape(12.dp),
                                singleLine    = true,
                                isError       = inviteError != null,
                                colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2EAA84))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick  = { viewModel.inviteMember(inviteEmail) },
                                modifier = Modifier.background(Color(0xFF2EAA84), RoundedCornerShape(12.dp))
                            ) { Icon(Icons.Default.PersonAdd, null, tint = Color.White) }
                        }
                        if (inviteError != null) {
                            Text(inviteError ?: "", color = Color(0xFFD03D2F), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun MemberRow(
    member:     MemberSummary,
    isMe:       Boolean = false,
    showDelete: Boolean = false,
    onDelete:   () -> Unit
) {
    val initial = member.firstName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val color   = when (member.role) {
        MemberSummary.Role.owner        -> Color(0xFF2EAA84)
        MemberSummary.Role.collaborator -> Color(0xFF1B63D1)
        else                            -> Color.Gray
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(44.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
            Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member.firstName ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (isMe) {
                    Surface(modifier = Modifier.padding(start = 8.dp), color = Color(0xFFE6F4EF), shape = RoundedCornerShape(8.dp)) {
                        Text("Moi", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp, color = Color(0xFF2EAA84), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(member.email ?: "", color = Color.Gray, fontSize = 12.sp)
        }
        if (showDelete) {
            IconButton(
                onClick  = onDelete,
                modifier = Modifier.size(32.dp).background(Color(0xFFFEF2F1), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Close, null, tint = Color(0xFFD03D2F), modifier = Modifier.size(18.dp))
            }
        }
    }
}
