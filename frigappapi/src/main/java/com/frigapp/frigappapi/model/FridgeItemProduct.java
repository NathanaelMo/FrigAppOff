package com.frigapp.frigappapi.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;

/**
 * FridgeItemProduct
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-26T22:36:13.511413300+01:00[Europe/Paris]", comments = "Generator version: 7.4.0")
public class FridgeItemProduct {

  private UUID id;

  private String name;

  private String brand;

  private String imageUrl;

  public FridgeItemProduct id(UUID id) {
    this.id = id;
    return this;
  }

  @Valid
  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public FridgeItemProduct name(String name) {
    this.name = name;
    return this;
  }

  @Schema(name = "name", example = "Lait entier", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public FridgeItemProduct brand(String brand) {
    this.brand = brand;
    return this;
  }

  @Schema(name = "brand", example = "Lactel", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("brand")
  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public FridgeItemProduct imageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
    return this;
  }

  @Schema(name = "imageUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("imageUrl")
  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FridgeItemProduct that = (FridgeItemProduct) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        Objects.equals(brand, that.brand) &&
        Objects.equals(imageUrl, that.imageUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, brand, imageUrl);
  }

  @Override
  public String toString() {
    return "FridgeItemProduct{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", brand='" + brand + '\'' +
        ", imageUrl='" + imageUrl + '\'' +
        '}';
  }
}
