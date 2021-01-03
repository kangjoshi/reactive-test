package com.example.reactivetest.controller;

import com.example.reactivetest.domain.Payment;
import com.example.reactivetest.service.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/")
    public Flux<Payment> list() {
        return paymentService.list();
    }

    @PostMapping("/")
    public Mono<String> send(Mono<Payment> payment) {
        return paymentService.send(payment);
    }




}
