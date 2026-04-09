package com.frigapp.frigappapi.services;

import com.frigapp.frigappapi.model.FridgeItem;
import com.frigapp.frigappapi.model.FridgeItemRequest;
import com.frigapp.frigappapi.model.FridgeItemUpdateRequest;
import com.frigapp.frigappapi.repositories.ItemsRepository;
import com.frigapp.frigappapi.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ItemsService {

    private final ItemsRepository itemsRepository;
    private final ProductRepository productRepository;

    // ── Lister ───────────────────────────────────────────────────────────────

    public List<FridgeItem> getItems(UUID fridgeId) {
        UUID userId = getCurrentUserId();
        log.info("Service : Récupération des items du frigo {} pour l'user {}", fridgeId, userId);

        if (!itemsRepository.isMember(fridgeId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas membre de ce frigo");
        }

        return itemsRepository.findAllByFridgeId(fridgeId);
    }

    // ── Ajouter ───────────────────────────────────────────────────────────────

    public FridgeItem addItem(UUID fridgeId, FridgeItemRequest request) {
        UUID userId = getCurrentUserId();
        log.info("Service : Ajout d'un item au frigo {} par l'user {}", fridgeId, userId);

        if (!itemsRepository.isMember(fridgeId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas membre de ce frigo");
        }

        // Vérifie que le produit existe
        if (productRepository.findById(request.getProductId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable");
        }

        FridgeItem saved = itemsRepository.save(
                fridgeId,
                request.getProductId(),
                userId,
                request.getQuantity(),
                request.getExpiryDate()
        );
        itemsRepository.touchFridge(fridgeId);
        return saved;
    }

    // ── Modifier ──────────────────────────────────────────────────────────────

    public FridgeItem updateItem(UUID fridgeId, UUID itemId, FridgeItemUpdateRequest request) {
        UUID userId = getCurrentUserId();
        log.info("Service : Modification de l'item {} du frigo {} par l'user {}", itemId, fridgeId, userId);

        if (!itemsRepository.isMember(fridgeId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas membre de ce frigo");
        }

        if (!itemsRepository.isItemInFridge(itemId, fridgeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item introuvable dans ce frigo");
        }

        FridgeItem updated = itemsRepository.update(itemId, request.getQuantity(), request.getExpiryDate());
        itemsRepository.touchFridge(fridgeId);
        return updated;
    }

    // ── Supprimer ─────────────────────────────────────────────────────────────

    public void deleteItem(UUID fridgeId, UUID itemId) {
        UUID userId = getCurrentUserId();
        log.info("Service : Suppression de l'item {} du frigo {} par l'user {}", itemId, fridgeId, userId);

        if (!itemsRepository.isMember(fridgeId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas membre de ce frigo");
        }

        if (!itemsRepository.isItemInFridge(itemId, fridgeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item introuvable dans ce frigo");
        }

        itemsRepository.delete(itemId);
        itemsRepository.touchFridge(fridgeId);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private UUID getCurrentUserId() {
        return (UUID) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();
    }
}
