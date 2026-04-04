package com.frigapp.frigappapi.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.frigapp.frigappapi.model.FridgeItemProduct;
import com.frigapp.frigappapi.model.UserSummary;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * FridgeItem
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-26T22:36:13.511413300+01:00[Europe/Paris]", comments = "Generator version: 7.4.0")
public class FridgeItem {

  private UUID id;

  private FridgeItemProduct product;

  private Integer quantity;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate expiryDate;

  private Boolean urgent;

  private Integer daysUntilExpiry;

  private UserSummary addedBy;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public FridgeItem id(UUID id) {
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

  public FridgeItem product(FridgeItemProduct product) {
    this.product = product;
    return this;
  }

  /**
   * Get product
   * @return product
  */
  @Valid 
  @Schema(name = "product", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("product")
  public FridgeItemProduct getProduct() {
    return product;
  }

  public void setProduct(FridgeItemProduct product) {
    this.product = product;
  }

  public FridgeItem quantity(Integer quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * Get quantity
   * @return quantity
  */
  
  @Schema(name = "quantity", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("quantity")
  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public FridgeItem expiryDate(LocalDate expiryDate) {
    this.expiryDate = expiryDate;
    return this;
  }

  /**
   * Get expiryDate
   * @return expiryDate
  */
  @Valid 
  @Schema(name = "expiryDate", example = "Wed Mar 25 01:00:00 CET 2026", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("expiryDate")
  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
    this.expiryDate = expiryDate;
  }

  public FridgeItem urgent(Boolean urgent) {
    this.urgent = urgent;
    return this;
  }

  /**
   * true si le produit expire dans 3 jours ou moins
   * @return urgent
  */
  
  @Schema(name = "urgent", example = "true", description = "true si le produit expire dans 3 jours ou moins", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("urgent")
  public Boolean getUrgent() {
    return urgent;
  }

  public void setUrgent(Boolean urgent) {
    this.urgent = urgent;
  }

  public FridgeItem daysUntilExpiry(Integer daysUntilExpiry) {
    this.daysUntilExpiry = daysUntilExpiry;
    return this;
  }

  /**
   * Négatif si déjà expiré
   * @return daysUntilExpiry
  */
  
  @Schema(name = "daysUntilExpiry", example = "2", description = "Négatif si déjà expiré", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("daysUntilExpiry")
  public Integer getDaysUntilExpiry() {
    return daysUntilExpiry;
  }

  public void setDaysUntilExpiry(Integer daysUntilExpiry) {
    this.daysUntilExpiry = daysUntilExpiry;
  }

  public FridgeItem addedBy(UserSummary addedBy) {
    this.addedBy = addedBy;
    return this;
  }

  /**
   * Get addedBy
   * @return addedBy
  */
  @Valid 
  @Schema(name = "addedBy", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("addedBy")
  public UserSummary getAddedBy() {
    return addedBy;
  }

  public void setAddedBy(UserSummary addedBy) {
    this.addedBy = addedBy;
  }

  public FridgeItem createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
  */
  @Valid 
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public FridgeItem updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
  */
  @Valid 
  @Schema(name = "updatedAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FridgeItem fridgeItem = (FridgeItem) o;
    return Objects.equals(this.id, fridgeItem.id) &&
        Objects.equals(this.product, fridgeItem.product) &&
        Objects.equals(this.quantity, fridgeItem.quantity) &&
        Objects.equals(this.expiryDate, fridgeItem.expiryDate) &&
        Objects.equals(this.urgent, fridgeItem.urgent) &&
        Objects.equals(this.daysUntilExpiry, fridgeItem.daysUntilExpiry) &&
        Objects.equals(this.addedBy, fridgeItem.addedBy) &&
        Objects.equals(this.createdAt, fridgeItem.createdAt) &&
        Objects.equals(this.updatedAt, fridgeItem.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, product, quantity, expiryDate, urgent, daysUntilExpiry, addedBy, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FridgeItem {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    product: ").append(toIndentedString(product)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
    sb.append("    expiryDate: ").append(toIndentedString(expiryDate)).append("\n");
    sb.append("    urgent: ").append(toIndentedString(urgent)).append("\n");
    sb.append("    daysUntilExpiry: ").append(toIndentedString(daysUntilExpiry)).append("\n");
    sb.append("    addedBy: ").append(toIndentedString(addedBy)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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

