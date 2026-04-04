package com.frigapp.frigappapi.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.frigapp.frigappapi.model.MemberSummary;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * FridgesFridgeIdMembersGet200Response
 */

@JsonTypeName("_fridges__fridgeId__members_get_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-26T22:36:13.511413300+01:00[Europe/Paris]", comments = "Generator version: 7.4.0")
public class FridgesFridgeIdMembersGet200Response {

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    SUCCESS("success");

    private String value;

    StatusEnum(String value) {
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
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private StatusEnum status;

  @Valid
  private List<@Valid MemberSummary> data;

  public FridgesFridgeIdMembersGet200Response status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  
  @Schema(name = "status", example = "success", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public FridgesFridgeIdMembersGet200Response data(List<@Valid MemberSummary> data) {
    this.data = data;
    return this;
  }

  public FridgesFridgeIdMembersGet200Response addDataItem(MemberSummary dataItem) {
    if (this.data == null) {
      this.data = new ArrayList<>();
    }
    this.data.add(dataItem);
    return this;
  }

  /**
   * Get data
   * @return data
  */
  @Valid 
  @Schema(name = "data", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("data")
  public List<@Valid MemberSummary> getData() {
    return data;
  }

  public void setData(List<@Valid MemberSummary> data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FridgesFridgeIdMembersGet200Response fridgesFridgeIdMembersGet200Response = (FridgesFridgeIdMembersGet200Response) o;
    return Objects.equals(this.status, fridgesFridgeIdMembersGet200Response.status) &&
        Objects.equals(this.data, fridgesFridgeIdMembersGet200Response.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FridgesFridgeIdMembersGet200Response {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

