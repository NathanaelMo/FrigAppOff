package com.monnier.frigapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monnier.frigapp.ui.viewmodels.RegisterViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // --- 1. EN-TÊTE VERT ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(Color(0xFF2EAA84))
                .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackToLogin) {
                    Icon(Icons.Default.ChevronLeft, "Retour", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Column {
                    Text("Créer un compte", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Rejoins Frig'App en quelques secondes", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }

        Box(modifier = Modifier.padding(20.dp)) {
            // --- 2. MESSAGE DE SUCCÈS ---
            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(
                    visible = viewModel.isRegistered,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2EAA84))
                            Spacer(Modifier.width(12.dp))
                            Text("Inscription réussie ! Retour au login...", color = Color(0xFF2EAA84), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- 3. FORMULAIRE ---
            if (!viewModel.isRegistered) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Prénom
                        Column {
                            Text("Prénom", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = viewModel.name,
                                onValueChange = { viewModel.name = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !viewModel.isLoading
                            )
                        }

                        // Email
                        Column {
                            Text("Adresse email", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = viewModel.email,
                                onValueChange = { viewModel.email = it },
                                placeholder = { Text("ton@email.com") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !viewModel.isLoading
                            )
                        }

                        // Mot de passe
                        Column {
                            Text("Mot de passe", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = viewModel.password,
                                onValueChange = { viewModel.password = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = PasswordVisualTransformation(),
                                enabled = !viewModel.isLoading
                            )
                            // Barre de force dynamique liée au ViewModel
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(4) { index ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f).height(4.dp)
                                            .background(
                                                if (viewModel.passwordStrength > index) Color(0xFF2EAA84) else Color(0xFFE9ECEF),
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }

                        if (viewModel.errorMessage != null) {
                            Text(viewModel.errorMessage!!, color = Color.Red, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.onRegisterClick(onBackToLogin) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !viewModel.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EAA84))
                        ) {
                            if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Créer mon compte", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp),
            enabled = !viewModel.isLoading
        ) {
            Text("Déjà un compte ? Se connecter", color = Color.Gray)
        }
    }
}