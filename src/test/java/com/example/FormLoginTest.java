package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Collections;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;


@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
public class FormLoginTest {

    @Autowired
    private WebTestClient rest;

    @Test
    public void returnOwnUser() throws Exception {
        String sessionId = this.formLogin("user1", "user1");

        this.rest
                .get()
                .uri("/api/user/current")
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .isEqualTo("Hello user1!")
        ;

        this.rest
                .mutateWith(csrf())
                .post()
                .uri("/logout")
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().is3xxRedirection();

        this.rest
                .get()
                .uri("/api/user/current")
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().is3xxRedirection();
        ;

    }


    private String formLogin(String user, String password) {
        this.rest
                .get()
                .uri("/login")
                .exchange()
                .expectStatus().is2xxSuccessful();
        FluxExchangeResult<String> result = this.rest
                .mutateWith(csrf())
                .post()
                .uri("/login")
                .body(BodyInserters
                        .fromFormData(new FormData(user, password).toParamList()))
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/")
                .returnResult(String.class);

        return result.getResponseCookies().getFirst("SESSION").getValue();
    }

    public static final class FormData {

        private final String username;
        private final String password;
        private String usernameParam;
        private String passwordParam;

        public FormData(String username, String password) {
            this("username", username, "password", password);
        }

        public FormData(String usernameParam, String username, String passwordParam, String password) {
            org.springframework.util.Assert.notNull(usernameParam, "'usernameParam' must not be null");
            org.springframework.util.Assert.notNull(passwordParam, "'passwordParam' must not be null");

            org.springframework.util.Assert.notNull(username, "'username' must not be null");
            org.springframework.util.Assert.notNull(password, "'password' must not be null");

            this.usernameParam = usernameParam;
            this.passwordParam = passwordParam;

            this.username = username;
            this.password = password;
        }

        public MultiValueMap<String, String> toParamList() {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.addAll(usernameParam, Collections.singletonList(username));
            parameters.addAll(passwordParam, Collections.singletonList(password));
            return parameters;
        }
    }
}
