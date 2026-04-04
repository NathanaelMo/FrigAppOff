package com.frigapp.frigappapi.controller;

import com.frigapp.frigappapi.controller.api.MembersApi;
import com.frigapp.frigappapi.model.*;
import com.frigapp.frigappapi.services.MembersService;
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
public class MembersController implements MembersApi {

    private final MembersService membersService;

    /**
     * GET /fridges/{fridgeId}/members
     * Liste les membres du frigo.
     */
    @Override
    public ResponseEntity<FridgesFridgeIdMembersGet200Response> fridgesFridgeIdMembersGet(UUID fridgeId) {
        log.info("Requête reçue : liste des membres du frigo {}", fridgeId);
        List<MemberSummary> members = membersService.getMembers(fridgeId);

        FridgesFridgeIdMembersGet200Response response = new FridgesFridgeIdMembersGet200Response();
        response.setStatus(FridgesFridgeIdMembersGet200Response.StatusEnum.SUCCESS);
        response.setData(members);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /fridges/{fridgeId}/members/invite
     * Invite un utilisateur existant par email. Réservé au propriétaire.
     */
    @Override
    public ResponseEntity<FridgesFridgeIdMembersInvitePost201Response> fridgesFridgeIdMembersInvitePost(
            UUID fridgeId,
            InviteMemberRequest inviteMemberRequest) {

        log.info("Requête reçue : invitation de '{}' dans le frigo {}",
                inviteMemberRequest.getEmail(), fridgeId);
        MemberSummary member = membersService.inviteMember(fridgeId, inviteMemberRequest);

        FridgesFridgeIdMembersInvitePost201Response response = new FridgesFridgeIdMembersInvitePost201Response();
        response.setStatus(FridgesFridgeIdMembersInvitePost201Response.StatusEnum.SUCCESS);
        response.setData(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /fridges/{fridgeId}/members/{userId}
     * Retire un collaborateur du frigo. Réservé au propriétaire.
     */
    @Override
    public ResponseEntity<Void> fridgesFridgeIdMembersUserIdDelete(UUID fridgeId, UUID userId) {
        log.info("Requête reçue : suppression du membre {} du frigo {}", userId, fridgeId);
        membersService.removeMember(fridgeId, userId);
        return ResponseEntity.noContent().build();
    }
}
