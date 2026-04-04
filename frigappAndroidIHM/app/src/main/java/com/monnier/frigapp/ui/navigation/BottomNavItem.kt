package com.monnier.frigapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Fridges : BottomNavItem("fridge_list", Icons.Default.Kitchen, "Mes Frigos")
    object Scan : BottomNavItem("scan", Icons.Default.QrCodeScanner, "Scanner")
    object Profile : BottomNavItem("profile", Icons.Default.AccountCircle, "Profil")
}