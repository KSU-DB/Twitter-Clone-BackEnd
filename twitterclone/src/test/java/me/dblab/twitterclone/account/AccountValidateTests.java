package me.dblab.twitterclone.account;

import me.dblab.twitterclone.common.BaseControllerTest;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

public class AccountValidateTests extends BaseControllerTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final String accountUrl = "/api/users";
    AccountDto accountDto;

    @BeforeEach
    @DisplayName("유저 생성")
    public void setUp() {
        accountRepository.deleteAll().subscribe();
        accountDto = createAccountDto();

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    @DisplayName("유저 null 검증 테스트")
    public void null_test()  {

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.empty(), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("username 길이 검증 테스트")
    public void username_length_test()    {

        accountDto.setUsername("abcde");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

    }

    @Test
    @DisplayName("password 길이 검증 테스트")
    public void password_length_test()    {
        accountDto.setPassword("yang");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

    }

    @Test
    @DisplayName("email 정규 표현식 검증 테스트")
    public void email_regex_test() {
        // '@'가 빠졌을 시 BadRequest
        accountDto.setEmail("testgmail.com");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // '.'가 빠졌을 시 BadRequest
        accountDto.setEmail("test@gmailcom");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

    }

    @Test
    @DisplayName("password 정규표현식 검증 테스트")
    public void password_regex_test() {
        // 영어 소문자 없을 시 BadRequest
        accountDto.setPassword("YANGKEESEOK");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // 영어 대문자 없을 시 BadRequest
        accountDto.setPassword("yangkeeseok");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // 영어 숫자 없을 시 BadRequest
        accountDto.setPassword("Yangkeeseok");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // 영어 특수문자 없을 시 BadRequest
        accountDto.setPassword("Yangkeeseok3");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

    }
}

