package com.example.reactivetest.service;

import com.example.reactivetest.domain.Payment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PaymentService {

    public Flux<Payment> list() {
        Payment payment1 = new Payment(1, 1000);
        Payment payment2 = new Payment(2, 2500);

        return Flux.just(payment1, payment2);
    }

    public Mono<String> send(Mono<Payment> payment) {
        return Mono.empty();
    }

}
