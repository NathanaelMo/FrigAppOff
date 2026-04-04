package com.frigapp.frigappapi.repositories;

import com.frigapp.frigappapi.model.UserFull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcClient jdbc;

    private static final String FIND_BY_EMAIL = """
            SELECT id, first_name, email, password_hash, created_at
            FROM users WHERE email = :email
            """;

    private static final String EXISTS_BY_EMAIL = """
            SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)
            """;

    private static final String INSERT_USER = """
            INSERT INTO users (first_name, email, password_hash)
            VALUES (:firstName, :email, :passwordHash)
            RETURNING id, first_name, email, created_at
            """;

    public Optional<UserFull> findById(UUID id) {
        return jdbc.sql("SELECT id, first_name, email, created_at FROM users WHERE id = :id")
                .param("id", id)
                .query((rs, rowNum) -> {
                    UserFull u = new UserFull();
                    u.setId(UUID.fromString(rs.getString("id")));
                    u.setFirstName(rs.getString("first_name"));
                    u.setEmail(rs.getString("email"));
                    u.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    return u;
                })
                .optional();
    }

    public Optional<UserFull> findByEmail(String email) {
        return jdbc.sql(FIND_BY_EMAIL)
                .param("email", email)
                .query((rs, rowNum) -> {
                    UserFull u = new UserFull();
                    u.setId(UUID.fromString(rs.getString("id")));
                    u.setFirstName(rs.getString("first_name"));
                    u.setEmail(rs.getString("email"));
                    u.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    return u;
                })
                .optional();
    }

    // Retourne aussi le hash — usage interne Auth uniquement
    public Optional<String> findPasswordHashByEmail(String email) {
        return jdbc.sql("SELECT password_hash FROM users WHERE email = :email")
                .param("email", email)
                .query((rs, rowNum) -> rs.getString("password_hash"))
                .optional();
    }

    public boolean existsByEmail(String email) {
        return jdbc.sql(EXISTS_BY_EMAIL)
                .param("email", email)
                .query((rs, rowNum) -> rs.getBoolean(1))
                .single();
    }

    public UserFull save(String firstName, String email, String passwordHash) {
        return jdbc.sql(INSERT_USER)
                .param("firstName", firstName)
                .param("email", email)
                .param("passwordHash", passwordHash)
                .query((rs, rowNum) -> {
                    UserFull u = new UserFull();
                    u.setId(UUID.fromString(rs.getString("id")));
                    u.setFirstName(rs.getString("first_name"));
                    u.setEmail(rs.getString("email"));
                    u.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    return u;
                })
                .single();
    }
}