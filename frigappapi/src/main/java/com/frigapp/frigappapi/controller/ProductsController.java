package com.frigapp.frigappapi.controller;

import com.frigapp.frigappapi.controller.api.ProductsApi;
import com.frigapp.frigappapi.model.CreateProductRequest;
import com.frigapp.frigappapi.model.Product;
import com.frigapp.frigappapi.model.ProductsBarcodeBarcodeGet200Response;
import com.frigapp.frigappapi.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductsController implements ProductsApi {

    private final ProductService productService;

    /**
     * GET /products/barcode/{barcode}
     *
     * Cherche le produit en base locale, puis sur Open Food Facts si absent.
     * Met le résultat en cache dans la table products.
     *
     * @param barcode Code EAN-13
     * @return 200 + produit, ou 404 si introuvable partout
     */
    @Override
    public ResponseEntity<ProductsBarcodeBarcodeGet200Response> productsBarcodeBarcodeGet(String barcode) {
        log.info("Requête reçue : recherche produit par barcode {}", barcode);

        Product product = productService.getByBarcode(barcode);

        if (product == null) {
            log.info("Produit {} introuvable (base + OFF)", barcode);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Produit introuvable — saisissez les informations manuellement");
        }

        return ResponseEntity.ok(buildResponse(product));
    }

    /**
     * POST /products
     *
     * Crée un produit manuellement (source = "manual").
     * Utilisé quand aucun résultat n'est trouvé par scan.
     *
     * @param createProductRequest Corps de la requête (barcode + name obligatoires)
     * @return 201 + produit créé
     */
    @Override
    public ResponseEntity<ProductsBarcodeBarcodeGet200Response> productsPost(CreateProductRequest createProductRequest) {
        log.info("Requête reçue : création manuelle du produit '{}' (barcode: {})",
                createProductRequest.getName(), createProductRequest.getBarcode());

        Product product = productService.createManual(createProductRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(product));
    }

    /**
     * DELETE /products/{productId} : Supprimer un produit manuel
     * <p>
     * Supprime un produit uniquement s'il a été créé manuellement (source manual).
     *
     * @param productId (required)
     * @return Produit supprimé (status code 204)
     */
    @Override
    public ResponseEntity<HttpStatus> productsProductIdDelete(UUID productId) {
        log.info("Requête reçue : suppression du produit '{}'",
                productId);
        productService.deleteProduct(productId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ProductsBarcodeBarcodeGet200Response buildResponse(Product product) {
        ProductsBarcodeBarcodeGet200Response response = new ProductsBarcodeBarcodeGet200Response();
        response.setStatus(ProductsBarcodeBarcodeGet200Response.StatusEnum.SUCCESS);
        response.setData(product);
        return response;
    }
}
