package com.frigapp.frigappapi.services;

import com.frigapp.frigappapi.model.InviteMemberRequest;
import com.frigapp.frigappapi.model.MemberSummary;
import com.frigapp.frigappapi.repositories.MembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembersService {

    private final MembersRepository membersRepository;

    // ── Lister ───────────────────────────────────────────────────────────────

    public List<MemberSummary> getMembers(UUID fridgeId) {
        UUID userId = getCurrentUserId();
        log.info("Service : Récupération des membres du frigo {} pour l'user {}", fridgeId, userId);

        if (!membersRepository.isMember(fridgeId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas membre de ce frigo");
        }

        return membersRepository.findAllByFridgeId(fridgeId);
    }

    // ── Inviter ───────────────────────────────────────────────────────────────

    public MemberSummary inviteMember(UUID fridgeId, InviteMemberRequest request) {
        UUID userId = getCurrentUserId();
        log.info("Service : Invitation de '{}' dans le frigo {} par l'user {}",
                request.getEmail(), fridgeId, userId);

        try {
            // inviteByEmail retourne Optional.empty() si l'email est inconnu
            return membersRepository.inviteByEmail(fridgeId, request.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Aucun compte trouvé pour cet email"));
        } catch (DataIntegrityViolationException e) {
            // Contrainte UNIQUE (fridge_id, user_id) violée → déjà membre
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cet utilisateur est déjà membre de ce frigo");
        }
    }

    // ── Retirer ───────────────────────────────────────────────────────────────

    public void removeMember(UUID fridgeId, UUID targetUserId) {
        UUID userId = getCurrentUserId();
        log.info("Service : Suppression du membre {} du frigo {} par l'user {}",
                targetUserId, fridgeId, userId);

        if (!membersRepository.isOwner(fridgeId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul le propriétaire peut retirer des membres");
        }

        if (targetUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Le propriétaire ne peut pas se retirer lui-même");
        }

        membersRepository.removeMember(fridgeId, targetUserId);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private UUID getCurrentUserId() {
        return (UUID) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();
    }
}
