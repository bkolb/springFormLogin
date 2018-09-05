package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange().anyExchange().authenticated()
            .and()
            .httpBasic().disable()
            //.and()
            .formLogin()
            .and()
            .csrf().csrfTokenRepository(new CookieServerCsrfTokenRepository())
            .and()
            .logout()
        ;

        return http.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsRepository() {
        UserDetails user1 = User.withDefaultPasswordEncoder()
            .username("user1")
            .password("user1")
            .roles("USER")
            .build();
        UserDetails user2 = User.withDefaultPasswordEncoder()
            .username("user2")
            .password("user2")
            .roles("USER")
            .build();
        ;
        return new MapReactiveUserDetailsService(user1, user2);
    }
}

