package com.frigapp.frigappapi.repositories;

import com.frigapp.frigappapi.model.FridgeFull;
import com.frigapp.frigappapi.model.FridgesGet200Response;
import com.frigapp.frigappapi.model.FridgesPost201Response;
import com.frigapp.frigappapi.model.FridgeSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FridgeRepository {

    private final JdbcClient jdbc;

    // ── Requêtes SQL ─────────────────────────────────────────────────────────

    private static final String FIND_BY_ID = """
            SELECT
                f.id, f.name, f.owner_id, f.created_at, f.updated_at,
                fm.role,
                COUNT(DISTINCT fi.id)  AS item_count,
                COUNT(DISTINCT fm2.id) AS member_count,
                COUNT(DISTINCT fi2.id) FILTER (
                    WHERE fi2.expiry_date <= CURRENT_DATE + INTERVAL '3 days'
                ) AS urgent_count
            FROM fridges f
            JOIN fridge_members fm  ON fm.fridge_id = f.id
            JOIN fridge_members fm2 ON fm2.fridge_id = f.id
            LEFT JOIN fridge_items fi  ON fi.fridge_id = f.id
            LEFT JOIN fridge_items fi2 ON fi2.fridge_id = f.id
            WHERE f.id = :fridgeId
            GROUP BY f.id, f.name, f.owner_id, f.created_at, f.updated_at, fm.role
            """;

    private static final String FIND_ALL_BY_USER_ID = """
            SELECT
                f.id, f.name, f.owner_id, f.created_at, f.updated_at,
                fm.role,
                COUNT(DISTINCT fi.id)  AS item_count,
                COUNT(DISTINCT fm2.id) AS member_count,
                COUNT(DISTINCT fi2.id) FILTER (
                    WHERE fi2.expiry_date <= CURRENT_DATE + INTERVAL '3 days'
                ) AS urgent_count
            FROM fridges f
            JOIN fridge_members fm  ON fm.fridge_id = f.id AND fm.user_id = :userId
            JOIN fridge_members fm2 ON fm2.fridge_id = f.id
            LEFT JOIN fridge_items fi  ON fi.fridge_id = f.id
            LEFT JOIN fridge_items fi2 ON fi2.fridge_id = f.id
            GROUP BY f.id, f.name, f.owner_id, f.created_at, f.updated_at, fm.role
            ORDER BY f.updated_at DESC
            """;

    private static final String INSERT_FRIDGE = """
            INSERT INTO fridges (name, owner_id)
            VALUES (:name, :ownerId)
            RETURNING id
            """;

    private static final String INSERT_OWNER_MEMBER = """
            INSERT INTO fridge_members (fridge_id, user_id, role)
            VALUES (:fridgeId, :userId, 'owner')
            """;

    private static final String UPDATE_NAME = """
            UPDATE fridges
            SET name = :name, updated_at = now()
            WHERE id = :fridgeId
            """;

    private static final String DELETE_FRIDGE =
            "DELETE FROM fridges WHERE id = :id";

    private static final String REMOVE_MEMBER = """
            DELETE FROM fridge_members
            WHERE fridge_id = :fridgeId
            AND user_id = :userId
            AND role != 'owner'
            """;

    // ── Méthodes ─────────────────────────────────────────────────────────────

    public Optional<FridgesPost201Response> findById(UUID fridgeId) {
        return jdbc.sql(FIND_BY_ID)
                .param("fridgeId", fridgeId)
                .query((rs, rowNum) -> {
                    FridgeFull full = new FridgeFull();
                    full.setId(UUID.fromString(rs.getString("id")));
                    full.setName(rs.getString("name"));
                    full.setOwnerId(UUID.fromString(rs.getString("owner_id")));
                    full.setRole(FridgeFull.RoleEnum.fromValue(rs.getString("role")));
                    full.setItemCount(rs.getInt("item_count"));
                    full.setMemberCount(rs.getInt("member_count"));
                    full.setUrgentCount(rs.getInt("urgent_count"));
                    full.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    full.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));

                    FridgesPost201Response response = new FridgesPost201Response();
                    response.setStatus(FridgesPost201Response.StatusEnum.SUCCESS);
                    response.setData(full);
                    return response;
                })
                .optional();
    }

    public FridgesGet200Response findAllByUserId(UUID userId) {
        List<FridgeSummary> fridges = jdbc.sql(FIND_ALL_BY_USER_ID)
                .param("userId", userId)
                .query((rs, rowNum) -> {
                    FridgeSummary s = new FridgeSummary();
                    s.setId(UUID.fromString(rs.getString("id")));
                    s.setName(rs.getString("name"));
                    s.setRole(FridgeSummary.RoleEnum.fromValue(rs.getString("role")));
                    s.setItemCount(rs.getInt("item_count"));
                    s.setMemberCount(rs.getInt("member_count"));
                    s.setUrgentCount(rs.getInt("urgent_count"));
                    s.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                    return s;
                })
                .list();

        FridgesGet200Response response = new FridgesGet200Response();
        response.setStatus(FridgesGet200Response.StatusEnum.SUCCESS);
        response.setData(fridges);
        return response;
    }

    public FridgesPost201Response save(String name, UUID ownerId) {
        UUID fridgeId = jdbc.sql(INSERT_FRIDGE)
                .param("name", name)
                .param("ownerId", ownerId)
                .query((rs, rowNum) -> UUID.fromString(rs.getString("id")))
                .single();

        jdbc.sql(INSERT_OWNER_MEMBER)
                .param("fridgeId", fridgeId)
                .param("userId", ownerId)
                .update();

        return findById(fridgeId).orElseThrow();
    }

    public FridgesPost201Response updateNamebyId(UUID fridgeId, String name) {
        jdbc.sql(UPDATE_NAME)
                .param("fridgeId", fridgeId)
                .param("name", name)
                .update();
        return findById(fridgeId).orElseThrow();
    }

    public void deleteById(UUID fridgeId) {
        jdbc.sql(DELETE_FRIDGE)
                .param("id", fridgeId)
                .update();
    }

    public void removeMember(UUID fridgeId, UUID userId) {
        jdbc.sql(REMOVE_MEMBER)
                .param("fridgeId", fridgeId)
                .param("userId", userId)
                .update();
    }
}