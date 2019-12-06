package me.dblab.twitterclone.common;

import me.dblab.twitterclone.config.jwt.AuthenticationManager;
import me.dblab.twitterclone.config.jwt.SecurityContextRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AuthServerConfigTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ServerHttpSecurity serverHttpSecurity;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    SecurityContextRepository securityContextRepository;

    @Autowired
    AppProperties appProperties;

    @Test
    public void getAuthToken() {
        webTestClient
                .post()
                .uri("/oauth/token")
                .attribute("username", appProperties.getTestEmail())
                .attribute("password", appProperties.getTestPassword())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("access_token")
                .exists();
    }
}