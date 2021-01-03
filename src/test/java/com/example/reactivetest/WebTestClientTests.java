package com.example.reactivetest;

import com.example.reactivetest.controller.PaymentController;
import com.example.reactivetest.domain.Payment;
import com.example.reactivetest.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class WebTestClientTests {

    @Test
    public void create_test_1() {
        PaymentService paymentService = Mockito.mock(PaymentService.class);
        PaymentController paymentController = new PaymentController(paymentService);

        when(paymentService.list()).thenReturn(Flux.just(new Payment(1, 1000), new Payment(2, 2000)));

        WebTestClient
                .bindToController(paymentController)
                .build()
                .get()
                .uri("/payments/")
                .exchange()
                .expectStatus().isOk()
                .returnResult(Payment.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextMatches(e -> e.getId() == 1)
                .expectNextMatches(e -> e.getId() == 2)
                .expectComplete()
                .verify();
    }

}
