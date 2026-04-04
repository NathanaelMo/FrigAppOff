package com.frigapp.frigappapi.controller;

import com.frigapp.frigappapi.controller.api.ItemsApi;
import com.frigapp.frigappapi.model.*;
import com.frigapp.frigappapi.services.ItemsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ItemsController implements ItemsApi {

    private final ItemsService itemsService;

    /**
     * GET /fridges/{fridgeId}/items
     * Liste tous les items du frigo, triés par date de péremption.
     */
    @Override
    public ResponseEntity<FridgesFridgeIdItemsGet200Response> fridgesFridgeIdItemsGet(
            UUID fridgeId,
            String sort) {

        log.info("Requête reçue : liste des items du frigo {}", fridgeId);
        List<FridgeItem> items = itemsService.getItems(fridgeId);

        FridgesFridgeIdItemsGet200Response response = new FridgesFridgeIdItemsGet200Response();
        response.setStatus(FridgesFridgeIdItemsGet200Response.StatusEnum.SUCCESS);
        response.setData(items);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /fridges/{fridgeId}/items
     * Ajoute un produit au frigo.
     */
    @Override
    public ResponseEntity<FridgesFridgeIdItemsPost201Response> fridgesFridgeIdItemsPost(
            UUID fridgeId,
            FridgeItemRequest fridgeItemRequest) {

        log.info("Requête reçue : ajout d'un item au frigo {} (produit {})",
                fridgeId, fridgeItemRequest.getProductId());
        FridgeItem item = itemsService.addItem(fridgeId, fridgeItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildItemResponse(item));
    }

    /**
     * PUT /fridges/{fridgeId}/items/{itemId}
     * Modifie la quantité et/ou la DLC d'un item.
     */
    @Override
    public ResponseEntity<FridgesFridgeIdItemsPost201Response> fridgesFridgeIdItemsItemIdPut(
            UUID fridgeId,
            UUID itemId,
            FridgeItemUpdateRequest fridgeItemUpdateRequest) {

        log.info("Requête reçue : modification de l'item {} du frigo {}", itemId, fridgeId);
        FridgeItem item = itemsService.updateItem(fridgeId, itemId, fridgeItemUpdateRequest);
        return ResponseEntity.ok(buildItemResponse(item));
    }

    /**
     * DELETE /fridges/{fridgeId}/items/{itemId}
     * Retire un item du frigo.
     */
    @Override
    public ResponseEntity<Void> fridgesFridgeIdItemsItemIdDelete(UUID fridgeId, UUID itemId) {
        log.info("Requête reçue : suppression de l'item {} du frigo {}", itemId, fridgeId);
        itemsService.deleteItem(fridgeId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private FridgesFridgeIdItemsPost201Response buildItemResponse(FridgeItem item) {
        FridgesFridgeIdItemsPost201Response response = new FridgesFridgeIdItemsPost201Response();
        response.setStatus(FridgesFridgeIdItemsPost201Response.StatusEnum.SUCCESS);
        response.setData(item);
        return response;
    }
}
