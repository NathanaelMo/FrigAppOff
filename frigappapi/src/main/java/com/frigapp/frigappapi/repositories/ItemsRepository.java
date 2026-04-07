package com.frigapp.frigappapi.repositories;

import com.frigapp.frigappapi.model.FridgeItem;
import com.frigapp.frigappapi.model.FridgeItemProduct;
import com.frigapp.frigappapi.model.UserSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemsRepository {

    private final JdbcClient jdbc;

    private static final String FIND_ALL = """
            SELECT
                fi.id, fi.quantity, fi.expiry_date, fi.created_at, fi.updated_at,
                (fi.expiry_date - CURRENT_DATE)         AS days_until_expiry,
                (fi.expiry_date - CURRENT_DATE) <= 3    AS urgent,
                p.id        AS product_id,
                p.name      AS product_name,
                p.brand,
                p.image_url,
                u.id         AS added_by_id,
                u.first_name AS added_by_name,
                u.email      AS added_by_email
            FROM fridge_items fi
            JOIN products p ON p.id = fi.product_id
            JOIN users    u ON u.id = fi.added_by
            WHERE fi.fridge_id = :fridgeId
            ORDER BY fi.expiry_date ASC
            """;

    private static final String FIND_BY_ID = """
            SELECT
                fi.id, fi.quantity, fi.expiry_date, fi.created_at, fi.updated_at,
                (fi.expiry_date - CURRENT_DATE)         AS days_until_expiry,
                (fi.expiry_date - CURRENT_DATE) <= 3    AS urgent,
                p.id        AS product_id,
                p.name      AS product_name,
                p.brand,
                p.image_url,
                u.id         AS added_by_id,
                u.first_name AS added_by_name,
                u.email      AS added_by_email
            FROM fridge_items fi
            JOIN products p ON p.id = fi.product_id
            JOIN users    u ON u.id = fi.added_by
            WHERE fi.id = :itemId
            """;

    private static final String INSERT = """
            INSERT INTO fridge_items (fridge_id, product_id, added_by, quantity, expiry_date)
            VALUES (:fridgeId, :productId, :addedBy, :quantity, :expiryDate)
            RETURNING id
            """;

    private static final String UPDATE = """
            UPDATE fridge_items
            SET quantity    = COALESCE(:quantity,   quantity),
                expiry_date = COALESCE(:expiryDate, expiry_date),
                updated_at  = now()
            WHERE id = :itemId
            """;

    private static final String DELETE = """
            DELETE FROM fridge_items WHERE id = :itemId
            """;

    private static final String IS_MEMBER = """
            SELECT EXISTS(
                SELECT 1 FROM fridge_members
                WHERE fridge_id = :fridgeId AND user_id = :userId
            )
            """;

    private static final String IS_ITEM_IN_FRIDGE = """
            SELECT EXISTS(
                SELECT 1 FROM fridge_items
                WHERE id = :itemId AND fridge_id = :fridgeId
            )
            """;

    // ── Méthodes ──────────────────────────────────────────────────────────────

    public List<FridgeItem> findAllByFridgeId(UUID fridgeId) {
        return jdbc.sql(FIND_ALL)
                .param("fridgeId", fridgeId)
                .query((rs, rowNum) -> mapRow(rs))
                .list();
    }

    public Optional<FridgeItem> findById(UUID itemId) {
        return jdbc.sql(FIND_BY_ID)
                .param("itemId", itemId)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    public FridgeItem save(UUID fridgeId, UUID productId, UUID addedBy,
                           int quantity, LocalDate expiryDate) {
        UUID itemId = jdbc.sql(INSERT)
                .param("fridgeId", fridgeId)
                .param("productId", productId)
                .param("addedBy", addedBy)
                .param("quantity", quantity)
                .param("expiryDate", expiryDate)
                .query((rs, rowNum) -> UUID.fromString(rs.getString("id")))
                .single();
        return findById(itemId).orElseThrow();
    }

    public FridgeItem update(UUID itemId, Integer quantity, LocalDate expiryDate) {
        jdbc.sql(UPDATE)
                .param("itemId", itemId)
                .param("quantity", quantity)
                .param("expiryDate", expiryDate)
                .update();
        return findById(itemId).orElseThrow();
    }

    public void delete(UUID itemId) {
        jdbc.sql(DELETE)
                .param("itemId", itemId)
                .update();
    }

    public boolean isMember(UUID fridgeId, UUID userId) {
        return jdbc.sql(IS_MEMBER)
                .param("fridgeId", fridgeId)
                .param("userId", userId)
                .query((rs, rowNum) -> rs.getBoolean(1))
                .single();
    }

    public boolean isItemInFridge(UUID itemId, UUID fridgeId) {
        return jdbc.sql(IS_ITEM_IN_FRIDGE)
                .param("itemId", itemId)
                .param("fridgeId", fridgeId)
                .query((rs, rowNum) -> rs.getBoolean(1))
                .single();
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private FridgeItem mapRow(ResultSet rs) throws SQLException {
        // Produit
        FridgeItemProduct product = new FridgeItemProduct();
        product.setId(UUID.fromString(rs.getString("product_id")));
        product.setName(rs.getString("product_name"));
        product.setBrand(rs.getString("brand"));
        product.setImageUrl(rs.getString("image_url"));

        // Ajouté par
        UserSummary addedBy = new UserSummary();
        addedBy.setId(UUID.fromString(rs.getString("added_by_id")));
        addedBy.setFirstName(rs.getString("added_by_name"));
        addedBy.setEmail(rs.getString("added_by_email"));

        // Item
        FridgeItem item = new FridgeItem();
        item.setId(UUID.fromString(rs.getString("id")));
        item.setProduct(product);
        item.setQuantity(rs.getInt("quantity"));
        item.setExpiryDate(rs.getObject("expiry_date", LocalDate.class));
        item.setDaysUntilExpiry(rs.getInt("days_until_expiry"));
        item.setUrgent(rs.getBoolean("urgent"));
        item.setAddedBy(addedBy);
        item.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        item.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        return item;
    }
}
