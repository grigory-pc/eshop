package ru.yandex.practicum.eshop.payment.service.controller;

import javax.annotation.processing.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.payment.service.dto.CreatePaymentByCartIdResponseOk;
import ru.yandex.practicum.eshop.payment.service.service.PaymentService;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
           comments = "Generator version: 7.12.0")
@Controller
@RequestMapping("${openapi..base-path:}")
@RequiredArgsConstructor
public class DefaultApiController implements DefaultApi {
  private final PaymentService paymentService;

  @Override
  public Mono<ResponseEntity<CreatePaymentByCartIdResponseOk>> createPaymentByCartId(
      @RequestParam(value = "cartId") Long cartId,
      ServerWebExchange exchange) {

    return paymentService.buyItems(cartId)
                         .map(orderId -> {
                           CreatePaymentByCartIdResponseOk response
                               = new CreatePaymentByCartIdResponseOk();
                           response.setOrderId(orderId);
                           return response;
                         })
                         .flatMap(response -> Mono.just(ResponseEntity.ok(response)));
  }
}