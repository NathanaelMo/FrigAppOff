package com.frigapp.frigappapi.services;

import com.frigapp.frigappapi.model.*;
import com.frigapp.frigappapi.repositories.FridgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FridgeService {

    private final FridgeRepository fridgeRepository;

    public void deleteFridge(UUID fridgeId) {
        UUID currentUserId = getCurrentUserId();
        log.info("Service : Demande de suppression du frigo {} par l'utilisateur {}", fridgeId, currentUserId);

        FridgesPost201Response fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> {
                    log.warn("Service : Échec de la suppression, le frigo {} est introuvable", fridgeId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Frigo introuvable");
                });

        FridgeFull fridgeData = fridge.getData();
        if (fridgeData == null || !currentUserId.equals(fridgeData.getOwnerId())) {
            log.error("Alerte de sécurité : L'utilisateur {} a tenté de supprimer le frigo {}", currentUserId, fridgeId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas le propriétaire de ce frigo");
        }

        fridgeRepository.deleteById(fridgeId);
        log.info("Service : Le frigo {} a été supprimé avec succès.", fridgeId);
    }

    public FridgesPost201Response getFridgeByID(UUID fridgeId) {
        UUID currentUserId = getCurrentUserId();
        log.info("Service : Traitement de la demande pour récupérer le frigo {}", fridgeId);

        return fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> {
                    log.warn("Service : Frigo {} introuvable en base de données", fridgeId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Le frigo demandé n'existe pas");
                });
    }

    public void deleteUserFromFridge(UUID fridgeId) {
        UUID currentUserId = getCurrentUserId();
        log.info("Service : Traitement de la demande pour supprimer le current user {} dans le frigo {}", currentUserId, fridgeId);

        FridgesPost201Response fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> {
                    log.warn("Service : Échec de la suppression du user {}, le frigo {} est introuvable", currentUserId, fridgeId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Frigo introuvable");
                });

        FridgeFull fridgeData = fridge.getData();
        if (fridgeData == null || currentUserId.equals(fridgeData.getOwnerId())) {
            log.error("Alerte de sécurité : L'utilisateur {} a tenté de quitter le frigo {}", currentUserId, fridgeId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous êtes le propriétaire de ce frigo");
        }

        fridgeRepository.removeMember(fridgeId, currentUserId);
        log.info("Service : Le user {} a été supprimé du frigo {} avec succès.", currentUserId, fridgeId);
    }

    public FridgesPost201Response updateFridgeName(UUID fridgeId, FridgeRequest fridgeRequest) {
        UUID currentUserId = getCurrentUserId();
        log.info("Service : Traitement de la demande de la part de l'user {} pour renommer le frigo {} en : {}", currentUserId, fridgeId, fridgeRequest.getName());

        FridgesPost201Response fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> {
                    log.warn("Service : Échec du renommage du frigo par l'user {}, le frigo {} est introuvable", currentUserId, fridgeId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Frigo introuvable");
                });

        FridgeFull fridgeData = fridge.getData();
        if (fridgeData == null || !currentUserId.equals(fridgeData.getOwnerId())) {
            log.error("Alerte de sécurité : L'utilisateur {} a tenté de renommer le frigo {}", currentUserId, fridgeId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas le propriétaire de ce frigo");
        }

        return fridgeRepository.updateNamebyId(fridgeId, fridgeRequest.getName());
    }

    public FridgesGet200Response getFridges() {
        UUID currentUserId = getCurrentUserId();
        log.info("Service : Récupération de tous les frigos pour l'utilisateur {}", currentUserId);
        return fridgeRepository.findAllByUserId(currentUserId);
    }

    public FridgesPost201Response PostNewFridge(FridgeRequest fridgeRequest) {
        UUID currentUserId = getCurrentUserId();
        log.info("Service : Création d'un nouveau frigo '{}' par l'utilisateur {}", fridgeRequest.getName(), currentUserId);
        return fridgeRepository.save(fridgeRequest.getName(), currentUserId);
    }

    private UUID getCurrentUserId() {
        return (UUID) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();
    }
}