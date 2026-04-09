package com.frigapp.frigappapi.repositories;

import com.frigapp.frigappapi.model.MemberSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MembersRepository {

    private final JdbcClient jdbc;

    private static final String FIND_ALL = """
            SELECT
                fm.id, fm.user_id, fm.role, fm.joined_at,
                u.first_name, u.email
            FROM fridge_members fm
            JOIN users u ON u.id = fm.user_id
            WHERE fm.fridge_id = :fridgeId
            ORDER BY
                CASE fm.role WHEN 'owner' THEN 0 ELSE 1 END,
                fm.joined_at ASC
            """;

    private static final String IS_OWNER = """
            SELECT EXISTS(
                SELECT 1 FROM fridge_members
                WHERE fridge_id = :fridgeId AND user_id = :userId AND role = 'owner'
            )
            """;

    private static final String IS_MEMBER = """
            SELECT EXISTS(
                SELECT 1 FROM fridge_members
                WHERE fridge_id = :fridgeId AND user_id = :userId
            )
            """;

    private static final String FIND_USER_ID_BY_EMAIL = """
            SELECT id FROM users WHERE email = :email
            """;

    private static final String INSERT_MEMBER = """
            INSERT INTO fridge_members (fridge_id, user_id, role)
            VALUES (:fridgeId, :userId, 'collaborator')
            """;

    private static final String DELETE_COLLABORATOR = """
            DELETE FROM fridge_members
            WHERE fridge_id = :fridgeId AND user_id = :userId AND role != 'owner'
            """;

    private static final String FIND_MEMBER_BY_USER = """
            SELECT
                fm.id, fm.user_id, fm.role, fm.joined_at,
                u.first_name, u.email
            FROM fridge_members fm
            JOIN users u ON u.id = fm.user_id
            WHERE fm.fridge_id = :fridgeId AND fm.user_id = :userId
            """;

    // ── Méthodes ──────────────────────────────────────────────────────────────

    public List<MemberSummary> findAllByFridgeId(UUID fridgeId) {
        return jdbc.sql(FIND_ALL)
                .param("fridgeId", fridgeId)
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    public boolean isOwner(UUID fridgeId, UUID userId) {
        return jdbc.sql(IS_OWNER)
                .param("fridgeId", fridgeId)
                .param("userId", userId)
                .query((rs, rowNum) -> rs.getBoolean(1))
                .single();
    }

    public boolean isMember(UUID fridgeId, UUID userId) {
        return jdbc.sql(IS_MEMBER)
                .param("fridgeId", fridgeId)
                .param("userId", userId)
                .query((rs, rowNum) -> rs.getBoolean(1))
                .single();
    }

    /**
     * Cherche un utilisateur par email et l'ajoute comme collaborateur.
     *
     * @return Le nouveau MemberSummary créé, ou Optional.empty() si l'email est inconnu.
     */
    public Optional<MemberSummary> inviteByEmail(UUID fridgeId, String email) {
        Optional<UUID> userId = jdbc.sql(FIND_USER_ID_BY_EMAIL)
                .param("email", email)
                .query((rs, rowNum) -> UUID.fromString(rs.getString("id")))
                .optional();

        if (userId.isEmpty()) return Optional.empty();

        jdbc.sql(INSERT_MEMBER)
                .param("fridgeId", fridgeId)
                .param("userId", userId.get())
                .update();

        return jdbc.sql(FIND_MEMBER_BY_USER)
                .param("fridgeId", fridgeId)
                .param("userId", userId.get())
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    public void removeMember(UUID fridgeId, UUID userId) {
        jdbc.sql(DELETE_COLLABORATOR)
                .param("fridgeId", fridgeId)
                .param("userId", userId)
                .update();
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private MemberSummary mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        MemberSummary m = new MemberSummary();
        m.setId(UUID.fromString(rs.getString("id")));
        m.setUserId(UUID.fromString(rs.getString("user_id")));
        m.setFirstName(rs.getString("first_name"));
        m.setEmail(rs.getString("email"));
        m.setRole(MemberSummary.RoleEnum.fromValue(rs.getString("role")));
        m.setJoinedAt(rs.getObject("joined_at", OffsetDateTime.class));
        return m;
    }
}
