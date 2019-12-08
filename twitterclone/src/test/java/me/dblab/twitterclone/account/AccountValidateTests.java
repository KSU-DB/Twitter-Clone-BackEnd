package me.dblab.twitterclone.account;

import me.dblab.twitterclone.common.AppProperties;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AccountValidateTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AppProperties appProperties;

    private final String accountUrl = "/api/users";

    private final String BEARER = "Bearer ";

    @Test
    public void null_검증_테스트()  {
        AccountDto accountDto = AccountDto.builder().build();

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .is5xxServerError();

    }

    @Test
    public void username_length_검증_테스트()    {

        // 길이 8 미만 일시 BadRequest
        AccountDto accountDto = createAccountDto();
        accountDto.setUsername("abcde");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // 유효한 username
        accountDto = createAccountDto();
        accountDto.setUsername("yangkeeseok");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    public void password_length_검증_테스트()    {

        // 길이 10 미만 일시 BadRequest
        AccountDto accountDto = createAccountDto();
        accountDto.setPassword("yang");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

    }

    @Test
    public void email_regex_검증_테스트() {

        // '@'가 빠졌을 시 BadRequest
        AccountDto accountDto = createAccountDto();
        accountDto.setEmail("testgmail.com");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // '.'가 빠졌을 시 BadRequest
        accountDto = createAccountDto();
        accountDto.setEmail("test@gmailcom");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // 유효한 email
        accountDto = createAccountDto();
        accountDto.setEmail("test@gmail.com");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    public void password_regex_검증_테스트() {

        // 영어 소문자 없을 시 BadRequest
        AccountDto accountDto = createAccountDto();
        accountDto.setPassword("YANGKEESEOK");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // 영어 대문자 없을 시 BadRequest
        accountDto = createAccountDto();
        accountDto.setPassword("yangkeeseok");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // 영어 숫자 없을 시 BadRequest
        accountDto = createAccountDto();
        accountDto.setPassword("Yangkeeseok");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // 영어 특수문자 없을 시 BadRequest
        accountDto = createAccountDto();
        accountDto.setPassword("Yangkeeseok3");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        // 유효한 패스워드
        accountDto = createAccountDto();
        accountDto.setPassword("Yangkeeseok3!@");

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isCreated();
    }

    private AccountDto createAccountDto() {
        return AccountDto.builder()
                .username(appProperties.getTestUsername())
                .nickname(appProperties.getTestNickname())
                .email(appProperties.getTestEmail())
                .password(appProperties.getTestPassword())
                .birthDate(Date.from(Instant.now()))
                .createdDate(LocalDateTime.now())
                .roles(Arrays.asList(Role.USER))
                .build();
    }

}

