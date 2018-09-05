package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
public class DummyController {

    @GetMapping("/**")
    public Mono<String> sayHello(Mono<Principal> pM) {
        return pM
                .map(Principal::getName)
                .map(n -> "Hello " + n + "!");
    }
}
