package com.frigapp.frigappapi.controller;

import com.frigapp.frigappapi.controller.api.FridgesApi;
import com.frigapp.frigappapi.model.*;
import com.frigapp.frigappapi.services.FridgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FridgesController implements FridgesApi {

    private final FridgeService fridgeService;

    /**
     * récupère une liste de tout les frigos dont l'utilisateur est le propriétaire
     *
     * @return ResponseEntity<FridgesGet200Response> Une réponse HTTP 200 si le get est un succès qui contient la liste de tout les friigos.
     */
    @Override
    public ResponseEntity<FridgesGet200Response> fridgesGet() {
        log.info("Requête reçue : récupération des infos de tout les frigos");
        return ResponseEntity.ok(fridgeService.getFridges());
    }

    /**
     * créé un nouveau frigo ou le proprietaire est l'user ayant envoyé la requête
     *
     * @param fridgeRequest  c'est la requête a envoyer pour effectuer la création du frigo
     * @return ResponseEntity<FridgesGet201Response> Une réponse HTTP 201 si le post est un succès qui contient toutes les données du nouveau frigo.
     */
    @Override
    public ResponseEntity<FridgesPost201Response> fridgesPost(FridgeRequest fridgeRequest) {
        log.info("Requête reçue : création d'un nouveau frigo avec la requête : nom du frigo {}", fridgeRequest.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(fridgeService.PostNewFridge(fridgeRequest));
    }

    /**
     * récupère les infos d'un frigo spécifique en fonction de l'id passer en paramètre
     *
     * @param fridgeId L'identifiant unique (UUID) du frigo à récupérer.
     * @return ResponseEntity<FridgesPost201Response> Une réponse HTTP 200 si le get du frigo est un succès avec toute les données du frigo obtenu.
     */
    @Override
    public ResponseEntity<FridgesPost201Response> fridgesFridgeIdGet(UUID fridgeId) {
        log.info("Requête reçue : récupération des infos du frigo avec l'ID {}", fridgeId);
        return ResponseEntity.ok(fridgeService.getFridgeByID(fridgeId));
    }

    /**
     *  renomme le nom du frigo ayant l'id donné en paramètre
     *
     * @param fridgeId L'identifiant unique (UUID) du frigo à récupérer.
     * @param fridgeRequest la requête pour mettre a jour le nom du frigo
     * @return ResponseEntity<FridgesPost201Response> Une réponse HTTP 200 si le get du frigo est un succès avec toute les données du frigo renommer.
     */
    @Override
    public ResponseEntity<FridgesPost201Response> fridgesFridgeIdPut(UUID fridgeId, FridgeRequest fridgeRequest) {
        log.info("Requête reçue : Renommer le frigo avec l'ID {}  pour le nom : {}", fridgeId, fridgeRequest.getName());
        return ResponseEntity.ok(fridgeService.updateFridgeName(fridgeId, fridgeRequest));
    }

    /**
     * Supprime un frigo et toutes ses données associées.
     * Cette action est définitive et est réservée au propriétaire du frigo.
     *
     * @param fridgeId L'identifiant unique (UUID) du frigo à supprimer.
     * @return ResponseEntity<Void> Une réponse HTTP 204 (No Content) si la suppression est un succès.
     */
    @Override
    public ResponseEntity<Void> fridgesFridgeIdDelete(UUID fridgeId) {
        log.info("Requête reçue : Suppression du frigo avec l'ID {}", fridgeId);
        fridgeService.deleteFridge(fridgeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * enlève le user de la liste des utilisateurs appartenant au frigo avec l'id donnée en paramètre
     *
     * @param fridgeId L'identifiant unique (UUID) du frigo à récupérer.
     * @return ResponseEntity<Void> Une réponse HTTP 204 (No Content) si la suppression de l'utilisateur dans le frigo est un succès.
     */
    @Override
    public ResponseEntity<Void> fridgesFridgeIdLeaveDelete(UUID fridgeId) {
        log.info("Requête reçue : Suppression de user dans le frigo avec l'ID {}", fridgeId);
        fridgeService.deleteUserFromFridge(fridgeId);
        return ResponseEntity.noContent().build();
    }
}