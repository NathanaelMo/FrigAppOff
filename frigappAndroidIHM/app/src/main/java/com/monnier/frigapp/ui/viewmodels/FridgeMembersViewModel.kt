package com.monnier.frigapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monnier.frigapp.FrigApplication
import com.monnier.frigapp.data.repository.MembersResult
import com.monnier.frigapp.generate.model.MemberSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FridgeMembersViewModel(application: Application) : AndroidViewModel(application) {

    private val membersRepository = (application as FrigApplication).membersRepository

    private val _members     = MutableStateFlow<List<MemberSummary>>(emptyList())
    val members = _members.asStateFlow()

    private val _isLoading   = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _inviteError  = MutableStateFlow<String?>(null)
    val inviteError = _inviteError.asStateFlow()

    private val _inviteSuccess = MutableStateFlow(false)
    val inviteSuccess = _inviteSuccess.asStateFlow()

    private var fridgeUuid: UUID? = null

    fun loadMembers(fridgeId: String) {
        fridgeUuid = runCatching { UUID.fromString(fridgeId) }.getOrNull() ?: run {
            _errorMessage.value = "Identifiant de frigo invalide"
            _isLoading.value    = false
            return
        }

        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null

            when (val result = membersRepository.getMembers(fridgeUuid!!)) {
                is MembersResult.Success -> _members.value = result.data
                is MembersResult.Error   -> _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun inviteMember(email: String) {
        val fridgeId = fridgeUuid ?: return
        if (email.isBlank()) { _inviteError.value = "Saisis un email"; return }

        viewModelScope.launch {
            _inviteError.value   = null
            _inviteSuccess.value = false

            when (val result = membersRepository.inviteMember(fridgeId, email)) {
                is MembersResult.Success -> {
                    _members.value       = _members.value + result.data
                    _inviteSuccess.value = true
                }
                is MembersResult.Error -> _inviteError.value = result.message
            }
        }
    }

    fun removeMember(userId: String) {
        val fridgeId = fridgeUuid ?: return
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull() ?: return

        viewModelScope.launch {
            when (val result = membersRepository.removeMember(fridgeId, userUuid)) {
                is MembersResult.Success ->
                    _members.value = _members.value.filter { it.userId?.toString() != userId }
                is MembersResult.Error ->
                    _errorMessage.value = result.message
            }
        }
    }

    fun resetInviteSuccess() { _inviteSuccess.value = false }
}
