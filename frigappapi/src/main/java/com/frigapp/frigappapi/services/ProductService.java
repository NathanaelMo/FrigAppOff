package com.frigapp.frigappapi.services;

import com.frigapp.frigappapi.model.CreateProductRequest;
import com.frigapp.frigappapi.model.FridgeFull;
import com.frigapp.frigappapi.model.FridgesPost201Response;
import com.frigapp.frigappapi.model.Product;
import com.frigapp.frigappapi.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion du référentiel produits.
 *
 * Stratégie de recherche par code-barres :
 * 1. Cherche dans la base locale (cache) → retour immédiat si trouvé
 * 2. Si absent : appel à l'API Open Food Facts
 * 3. Si trouvé sur OFF : sauvegarde en base et retourne le produit
 * 4. Si inconnu partout : retourne null → le contrôleur renvoie un 404
 *
 * La création manuelle (POST /products) est réservée aux cas où le scan
 * ne donne aucun résultat et l'utilisateur saisit les infos manuellement.
 */
@Slf4j
@Service
public class ProductService {

    private static final String OFF_URL =
            "https://world.openfoodfacts.org/api/v0/product/{barcode}.json" +
            "?fields=product_name,product_name_fr,brands,pnns_groups_2,image_front_url";

    private final ProductRepository productRepository;
    private final RestClient        restClient;

    public ProductService(ProductRepository productRepository, RestClient.Builder builder) {
        this.productRepository = productRepository;
        this.restClient        = builder.build();
    }

    // ── Recherche par code-barres ─────────────────────────────────────────────

    /**
     * Retourne le produit correspondant au code-barres, ou null s'il est introuvable.
     */
    public Product getByBarcode(String barcode) {
        // 1. Cache local
        return productRepository.findByBarcode(barcode)
                .orElseGet(() -> fetchFromOFFAndSave(barcode));
    }

    // ── Création manuelle ─────────────────────────────────────────────────────

    /**
     * Crée un produit manuellement (source = "manual").
     * Utilisé quand le scan ne donne aucun résultat sur OFF.
     *
     * @throws ResponseStatusException 409 si le code-barres est déjà en base
     */
    public Product createManual(CreateProductRequest req) {
        // Si le produit existe déjà on lève un conflit
        if (productRepository.findByBarcode(req.getBarcode()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un produit avec ce code-barres existe déjà");
        }

        String brand    = req.getBrand()    != null && req.getBrand().isPresent()    ? req.getBrand().get()    : null;
        String category = req.getCategory() != null && req.getCategory().isPresent() ? req.getCategory().get() : null;

        return productRepository.save(
                req.getBarcode(), req.getName(), brand, category, null, "manual"
        );
    }

    public void deleteProduct(UUID productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Le produit n'existe pas");
        } else if (!product.get().getSource().equals(Product.SourceEnum.MANUAL)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La source du produit n'est pas MANUAL");
        } else {
            productRepository.deleteById(productId);
        }

    }

    // ── Appel Open Food Facts ─────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Product fetchFromOFFAndSave(String barcode) {
        log.info("Produit {} absent du cache, appel Open Food Facts", barcode);

        try {
            Map<String, Object> body = restClient.get()
                    .uri(OFF_URL, barcode)
                    .retrieve()
                    .body(Map.class);

            if (body == null) {
                log.warn("Réponse vide de Open Food Facts pour {}", barcode);
                return null;
            }

            // status = 1 → trouvé, 0 → introuvable
            Object status = body.get("status");
            if (!Integer.valueOf(1).equals(status)) {
                log.info("Produit {} non référencé sur Open Food Facts", barcode);
                return null;
            }

            Map<String, Object> product = (Map<String, Object>) body.get("product");
            if (product == null) return null;

            // ── Nom (préférence française) ────────────────────────────────────
            String name = firstNonBlank(product.get("product_name_fr"), product.get("product_name"));
            if (name == null) {
                log.info("Produit {} trouvé sur OFF mais sans nom utilisable", barcode);
                return null;
            }

            // ── Marque (première de la liste séparée par virgules) ───────────
            String brand = stringify(product.get("brands"));
            if (brand != null && brand.contains(",")) {
                brand = brand.split(",")[0].trim();
            }

            // ── Catégorie (pnns_groups_2, déjà lisible en français) ──────────
            String category = stringify(product.get("pnns_groups_2"));
            if (category == null) {
                // Fallback : extraire la dernière entrée de categories_tags
                Object tags = product.get("categories_tags");
                if (tags instanceof List<?> list && !list.isEmpty()) {
                    String tag = stringify(list.get(list.size() - 1));
                    if (tag != null && tag.contains(":")) {
                        category = capitalize(tag.substring(tag.lastIndexOf(':') + 1).replace('-', ' '));
                    }
                }
            }

            // ── Image ─────────────────────────────────────────────────────────
            String imageUrl = stringify(product.get("image_front_url"));

            log.info("Produit {} trouvé sur OFF : '{}' ({})", barcode, name, brand);
            return productRepository.save(barcode, name, brand, category, imageUrl, "open_food_facts");

        } catch (Exception e) {
            log.error("Erreur lors de l'appel Open Food Facts pour {} : {}", barcode, e.getMessage());
            return null;
        }
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private String firstNonBlank(Object... values) {
        for (Object v : values) {
            String s = stringify(v);
            if (s != null && !s.isBlank()) return s;
        }
        return null;
    }

    private String stringify(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isBlank() ? null : s;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
