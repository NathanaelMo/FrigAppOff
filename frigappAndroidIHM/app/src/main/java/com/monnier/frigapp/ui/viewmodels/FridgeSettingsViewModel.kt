package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.FridgeResult
import com.monnier.frigapp.data.repository.MembersResult
import com.monnier.frigapp.generate.model.MemberSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel de l'écran paramètres d'un frigo.
 *
 * Responsabilités :
 * - Charger les détails du frigo (nom, rôle)
 * - Renommer le frigo (owner uniquement)
 * - Supprimer le frigo (owner uniquement)
 * - Quitter le frigo (collaborator uniquement)
 */
class FridgeSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val fridgeRepository   = (application as FrigApplication).fridgeRepository
    private val membersRepository  = (application as FrigApplication).membersRepository

    // ─── États ───────────────────────────────────────────────────────────────

    /** Nom affiché dans le champ éditable. */
    var fridgeName by mutableStateOf("")

    /** Rôle de l'utilisateur courant : "owner" ou "collaborator". */
    var userRole by mutableStateOf("collaborator")
        private set

    var isLoading    by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    /** Liste des membres du frigo — utilisée pour calculer le rôle réel de l'utilisateur. */
    private val _members = MutableStateFlow<List<MemberSummary>>(emptyList())
    val members = _members.asStateFlow()

    /** true = opération destructive en cours (delete/leave) → affiche un dialog. */
    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog = _showConfirmDialog.asStateFlow()

    private var currentFridgeId: UUID? = null

    // ─── Initialisation ───────────────────────────────────────────────────────

    /**
     * Charge les infos du frigo depuis l'API.
     * À appeler au démarrage via LaunchedEffect(fridgeId).
     */
    fun loadFridge(fridgeId: String) {
        val uuid = runCatching { UUID.fromString(fridgeId) }.getOrNull() ?: return
        currentFridgeId = uuid

        viewModelScope.launch {
            isLoading    = true
            errorMessage = null

            when (val result = fridgeRepository.getFridge(uuid)) {
                is FridgeResult.Success -> {
                    fridgeName = result.data.name ?: ""
                    userRole   = result.data.role?.value ?: "collaborator"
                }
                is FridgeResult.Error -> errorMessage = result.message
            }

            // Chargement des membres pour déterminer le rôle réel de l'utilisateur
            when (val result = membersRepository.getMembers(uuid)) {
                is MembersResult.Success -> _members.value = result.data
                is MembersResult.Error   -> { /* non bloquant : le rôle fallback reste disponible */ }
            }

            isLoading = false
        }
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    /**
     * Renomme le frigo via l'API.
     * Réservé au propriétaire.
     */
    fun renameFridge(onSuccess: () -> Unit) {
        val uuid = currentFridgeId ?: return
        if (fridgeName.isBlank()) {
            errorMessage = "Le nom ne peut pas être vide"
            return
        }

        viewModelScope.launch {
            isLoading    = true
            errorMessage = null

            when (val result = fridgeRepository.renameFridge(uuid, fridgeName.trim())) {
                is FridgeResult.Success -> {
                    fridgeName = result.data.name ?: fridgeName
                    onSuccess()
                }
                is FridgeResult.Error -> errorMessage = result.message
            }

            isLoading = false
        }
    }

    /**
     * Supprime le frigo et toutes ses données.
     * Réservé au propriétaire. Navigue vers la liste après succès.
     */
    fun deleteFridge(onSuccess: () -> Unit) {
        val uuid = currentFridgeId ?: return

        viewModelScope.launch {
            isLoading    = true
            errorMessage = null

            when (val result = fridgeRepository.deleteFridge(uuid)) {
                is FridgeResult.Success -> onSuccess()
                is FridgeResult.Error   -> errorMessage = result.message
            }

            isLoading = false
        }
    }

    /**
     * Retire l'utilisateur courant des membres du frigo.
     * Réservé aux collaborateurs. Navigue vers la liste après succès.
     */
    fun leaveFridge(onSuccess: () -> Unit) {
        val uuid = currentFridgeId ?: return

        viewModelScope.launch {
            isLoading    = true
            errorMessage = null

            when (val result = fridgeRepository.leaveFridge(uuid)) {
                is FridgeResult.Success -> onSuccess()
                is FridgeResult.Error   -> errorMessage = result.message
            }

            isLoading = false
        }
    }

    fun showConfirmDialog()  { _showConfirmDialog.value = true  }
    fun dismissConfirmDialog() { _showConfirmDialog.value = false }
}
