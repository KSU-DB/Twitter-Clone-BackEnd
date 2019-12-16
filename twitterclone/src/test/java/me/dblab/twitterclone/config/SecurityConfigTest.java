package me.dblab.twitterclone.config;

import me.dblab.twitterclone.account.AccountDto;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.common.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.BDDAssertions.then;

public class SecurityConfigTest extends BaseControllerTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    private final String baseUrl = "http://localhost:8080";
    private final String userUrl = "/api/users";

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll().subscribe();
    }

    @Test
    @DisplayName("PasswordEncoder 검증")
    public void encodeTest() {
        String password = "testpassword";
        String encode = passwordEncoder.encode(password);
        then(passwordEncoder.matches(password, encode)).isTrue();
    }

    @Test
    @DisplayName("허용한 Origin 에서 요청했을 때")
    public void corsTest() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().host("localhost").port(3000);
        UriComponents host = uriBuilder.build();
        UriComponents origin = uriBuilder.scheme("http").build();

        webTestClient
                .mutate().baseUrl(baseUrl).build()
                .post()
                .uri(userUrl)
                .header(HttpHeaders.HOST, host.toUriString())
                .header(HttpHeaders.ORIGIN, origin.toUriString())
                .header(HttpHeaders.REFERER, origin.toUriString() + "/signup")
                .body(Mono.just(createAccountDto()), AccountDto.class)
                .exchange()
                .expectStatus()
                .isCreated();
    }
}