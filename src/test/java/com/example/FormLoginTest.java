package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
public class FormLoginTest {

    @Autowired
    private WebTestClient rest;

    @Test
    public void formLoginWhenValidCredentialsThenSessionCreated() {
        FluxExchangeResult<String> result = this.rest
                .mutateWith(csrf())
                .post()
                .uri("/login")
                .body(BodyInserters
                        .fromFormData(new FormData("user1", "user1").toParamList()))
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/")
                .returnResult(String.class);

        assertThat(result.getResponseCookies().keySet()).contains("SESSION");
    }

    @Test
    @WithMockUser("user1")
    public void apiWhenWithMockUserThenSaysHello() throws Exception {
        this.rest.get()
                .uri("/api/user/current")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class).isEqualTo("Hello user1!");
    }

    @Test
    @WithMockUser
    public void logoutWhenSuccessThenDeletesSession() throws Exception {

        FluxExchangeResult<String> result = this.rest
                .mutateWith(csrf())
                .post()
                .uri("/logout")
                .cookie("SESSION", "any")
                .exchange().expectStatus()
                .is3xxRedirection().returnResult(String.class);

        assertThat(result.getResponseCookies().getFirst("SESSION").getMaxAge()).isEqualTo(
                Duration.ZERO);

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
