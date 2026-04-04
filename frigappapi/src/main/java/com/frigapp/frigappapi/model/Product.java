package com.frigapp.frigappapi.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.UUID;

import jakarta.annotation.Nullable;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.annotation.Generated;

/**
 * Product
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-26T22:36:13.511413300+01:00[Europe/Paris]", comments = "Generator version: 7.4.0")
public class Product {

  private UUID id;

  private String barcode;

  private String name;

  @Nullable
  private String brand;

  @Setter
  @Nullable
  private String category;

  @Nullable
  private URI imageUrl;

  /**
   * Gets or Sets source
   */
  public enum SourceEnum {
    OPEN_FOOD_FACTS("open_food_facts"),
    
    MANUAL("manual");

    private String value;

    SourceEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SourceEnum fromValue(String value) {
      for (SourceEnum b : SourceEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @Setter
  private SourceEnum source;

  @Setter
  private JsonNullable<Integer> suggestedExpiryDays = JsonNullable.<Integer>undefined();

  public Product id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  @Valid 
  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Product barcode(String barcode) {
    this.barcode = barcode;
    return this;
  }

  /**
   * Get barcode
   * @return barcode
  */
  
  @Schema(name = "barcode", example = "3017620422003", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("barcode")
  public String getBarcode() {
    return barcode;
  }

  public void setBarcode(String barcode) {
    this.barcode = barcode;
  }

  public Product name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  */
  
  @Schema(name = "name", example = "Nutella", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Product brand(String brand) {
    this.brand = brand;
    return this;
  }

  /**
   * Get brand
   * @return brand
  */
  
  @Schema(name = "brand", example = "Ferrero", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("brand")
  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public Product category(String category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
  */
  
  @Schema(name = "category", example = "Pâtes à tartiner", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("category")
  public @org.jspecify.annotations.Nullable String getCategory() {
    return category;
  }

    public Product imageUrl(URI imageUrl) {
    this.imageUrl = imageUrl;
    return this;
  }

  /**
   * Get imageUrl
   * @return imageUrl
  */
  @Valid 
  @Schema(name = "imageUrl", example = "https://images.openfoodfacts.org/images/products/301/762/042/2003/front.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("imageUrl")
  public @org.jspecify.annotations.Nullable URI getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(@org.jspecify.annotations.Nullable URI imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Product source(SourceEnum source) {
    this.source = source;
    return this;
  }

  /**
   * Get source
   * @return source
  */
  
  @Schema(name = "source", example = "open_food_facts", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("source")
  public SourceEnum getSource() {
    return source;
  }

    public Product suggestedExpiryDays(Integer suggestedExpiryDays) {
    this.suggestedExpiryDays = JsonNullable.of(suggestedExpiryDays);
    return this;
  }

  /**
   * Durée de conservation suggérée en jours selon la catégorie
   * @return suggestedExpiryDays
  */
  
  @Schema(name = "suggestedExpiryDays", example = "365", description = "Durée de conservation suggérée en jours selon la catégorie", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("suggestedExpiryDays")
  public JsonNullable<Integer> getSuggestedExpiryDays() {
    return suggestedExpiryDays;
  }

    @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Product product = (Product) o;
    return Objects.equals(this.id, product.id) &&
        Objects.equals(this.barcode, product.barcode) &&
        Objects.equals(this.name, product.name) &&
        Objects.equals(this.brand, product.brand) &&
        Objects.equals(this.category, product.category) &&
        Objects.equals(this.imageUrl, product.imageUrl) &&
        Objects.equals(this.source, product.source) &&
        equalsNullable(this.suggestedExpiryDays, product.suggestedExpiryDays);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, barcode, name, brand, category, imageUrl, source, suggestedExpiryDays);
  }

  private static <T> int hashCodeNullable(JsonNullable<T> a) {
    if (a == null) {
      return 1;
    }
    return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Product {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    barcode: ").append(toIndentedString(barcode)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    brand: ").append(toIndentedString(brand)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    imageUrl: ").append(toIndentedString(imageUrl)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    suggestedExpiryDays: ").append(toIndentedString(suggestedExpiryDays)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

