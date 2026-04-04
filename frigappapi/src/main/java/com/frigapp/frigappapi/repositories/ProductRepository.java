package com.frigapp.frigappapi.repositories;

import com.frigapp.frigappapi.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final JdbcClient jdbc;

    private static final String FIND_BY_ID = """
            SELECT id, barcode, name, brand, category, image_url, source
            FROM products WHERE id = :id
            """;

    private static final String FIND_BY_BARCODE = """
            SELECT id, barcode, name, brand, category, image_url, source
            FROM products WHERE barcode = :barcode
            """;

    private static final String INSERT_OR_UPDATE = """
            INSERT INTO products (barcode, name, brand, category, image_url, source)
            VALUES (:barcode, :name, :brand, :category, :imageUrl, :source)
            ON CONFLICT (barcode) DO UPDATE SET
                name      = EXCLUDED.name,
                brand     = EXCLUDED.brand,
                category  = EXCLUDED.category,
                image_url = EXCLUDED.image_url
            RETURNING id, barcode, name, brand, category, image_url, source
            """;

    private static final String DELETE_PRODUCT =
            "DELETE FROM products WHERE id = :id";

    /**
     * Cherche un produit par code-barres dans la base locale.
     *
     * @param id
     * @return Le produit s'il existe déjà en base, sinon Optional.empty()
     */
    public Optional<Product> findById(UUID id) {
        return jdbc.sql(FIND_BY_ID)
                .param("id", id)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    public Optional<Product> findByBarcode(String barcode) {
        return jdbc.sql(FIND_BY_BARCODE)
                .param("barcode", barcode)
                .query((rs, rowNum) -> mapRow(rs))
                .optional();
    }

    /**
     * Sauvegarde (ou met à jour) un produit en base.
     * Utilise ON CONFLICT pour éviter les doublons sur le code-barres.
     *
     * @param barcode  Code EAN-13
     * @param name     Nom du produit
     * @param brand    Marque (peut être null)
     * @param category Catégorie (peut être null)
     * @param imageUrl URL de l'image (peut être null)
     * @param source   Source : "open_food_facts" ou "manual"
     * @return Le produit tel qu'il est stocké en base
     */
    public Product save(String barcode, String name, String brand,
                        String category, String imageUrl, String source) {
        return jdbc.sql(INSERT_OR_UPDATE)
                .param("barcode", barcode)
                .param("name", name)
                .param("brand", brand)
                .param("category", category)
                .param("imageUrl", imageUrl)
                .param("source", source)
                .query((rs, rowNum) -> mapRow(rs))
                .single();
    }

    public void deleteById(UUID productId) {
        jdbc.sql(DELETE_PRODUCT)
                .param("id", productId)
                .update();
    }

    // ── Mapping ResultSet → Product ───────────────────────────────────────────

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(UUID.fromString(rs.getString("id")));
        p.setBarcode(rs.getString("barcode"));
        p.setName(rs.getString("name"));

        String brand = rs.getString("brand");
        p.setBrand(brand);

        String category = rs.getString("category");
        p.setCategory(category);

        String imageUrl = rs.getString("image_url");
        if (imageUrl != null) {
            try {
                p.setImageUrl(new URI(imageUrl));
            } catch (Exception e) {
                log.warn("URL image invalide pour le produit {}: {}", rs.getString("barcode"), imageUrl);
                p.setImageUrl(null);
            }
        } else {
            p.setImageUrl(null);
        }

        String source = rs.getString("source");
        if (source != null) {
            p.setSource(Product.SourceEnum.fromValue(source));
        }

        return p;
    }
}
