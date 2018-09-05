package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.security.Principal;

public class DummyController {

    @GetMapping("/api/sayHello")
    public Mono<String> sayHello(Mono<Principal> pM) {
        return pM
                .map(Principal::getName)
                .map(n -> "Hello " + n + "!");
    }
}
