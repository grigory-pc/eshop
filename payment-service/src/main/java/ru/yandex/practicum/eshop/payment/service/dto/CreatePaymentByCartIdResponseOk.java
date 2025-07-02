package ru.yandex.practicum.eshop.payment.service.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.processing.Generated;
import org.springframework.lang.Nullable;

/**
 * CreatePaymentByCartIdResponseOk
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
           comments = "Generator version: 7.12.0")
public class CreatePaymentByCartIdResponseOk {

  private @Nullable Long orderId;

  public CreatePaymentByCartIdResponseOk orderId(Long orderId) {
    this.orderId = orderId;
    return this;
  }

  /**
   * ID созданного заказа
   * @return orderId
   */

  @JsonProperty("orderId")
  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreatePaymentByCartIdResponseOk createPaymentByCartIdResponseOk
        = (CreatePaymentByCartIdResponseOk) o;
    return Objects.equals(this.orderId, createPaymentByCartIdResponseOk.orderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreatePaymentByCartIdResponseOk {\n");
    sb.append("    orderId: ").append(toIndentedString(orderId)).append("\n");
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

