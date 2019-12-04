package me.dblab.twitterclone.account;

import me.dblab.twitterclone.common.AppProperties;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AccountControllerTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    AppProperties appProperties;

    private final String accountUrl = "/api/users";

    private final String BEARER = "Bearer ";

    @Test
    public void 로그인_테스트() {
        Account account = createAccount();

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(account), Account.class)
                .exchange()
                .expectStatus()
                .isCreated();

        webTestClient.post()
                .uri(accountUrl + "/login")
                .body(Mono.just(account), Account.class)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void 유저_불러오기_테스트() {
        Account account = createAccount();

        Mono<Account> accountMono = Mono.justOrEmpty(account);
        webTestClient.post()
                .uri(accountUrl)
                .body(accountMono, Account.class)
                .exchange()
                .expectStatus()
                .isCreated();

        String jwt = BEARER + tokenProvider.generateToken(account);
        webTestClient.get()
                .uri(accountUrl + "/" + account.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void 유저_저장_테스트() {
        Account account = createAccount();

        String jwt = BEARER + tokenProvider.generateToken(account);

        webTestClient.post()
                .uri(accountUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(account), Account.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Account> byId = accountRepository.findById(account.getId());

        StepVerifier.create(byId)
                .assertNext(i -> assertThat(account).isNotNull())
                .verifyComplete();
    }

    @Test
    public void 유저_수정_테스트() {
        Account account = createAccount();
        Mono<Account> accountMono = Mono.justOrEmpty(account);
        String jwt = BEARER + tokenProvider.generateToken(account);
        String updateUsername = "updateUsername";

        webTestClient.post()
                .uri(accountUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(accountMono, Account.class)
                .exchange()
                .expectStatus().isCreated();


        accountMono.map(acc -> {
            acc.setUsername(updateUsername);
            return acc;
        }).subscribe();

        StepVerifier.create(accountMono)
                .assertNext(acc -> then(acc.getUsername()).isEqualTo(updateUsername))
                .verifyComplete();

        webTestClient.put()
                .uri(accountUrl + "/" + account.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(accountMono, Account.class)
                .exchange()
                .expectStatus().isOk();

        Mono<Account> byId = accountRepository.findById(account.getId());

        StepVerifier.create(byId)
                .assertNext(i -> {
                    then(account).isNotNull();
                    then(account.getEmail()).isEqualTo(appProperties.getTestEmail());
                    then(account.getUsername()).isEqualTo(updateUsername);
                })
                .verifyComplete();
    }

    @Test
    public void 유저_삭제_테스트() {
        Account account = createAccount();

        Mono<Account> accountMono = Mono.justOrEmpty(account);
        String jwt = BEARER + tokenProvider.generateToken(account);

        webTestClient.post()
                .uri(accountUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(accountMono, Account.class)
                .exchange()
                .expectStatus().isCreated();


        webTestClient.delete()
                .uri(accountUrl + "/" + account.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk();
    }

    private Account createAccount() {
        return Account.builder()
                .id(UUID.randomUUID().toString())
                .username(appProperties.getTestUsername())
                .nickname("nickname-test")
                .email(appProperties.getTestEmail())
                .password(appProperties.getTestPassword())
                .birthDate(Date.from(Instant.now()))
                .createdDate(LocalDateTime.now())
                .roles(Arrays.asList(Role.USER))
                .build();
    }

}
