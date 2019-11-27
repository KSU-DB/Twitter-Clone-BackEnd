package me.dblab.twitterclone.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AccountControllerTests {

    @Autowired
    WebTestClient webTestClient;

    final String uri = "/api/users";

    @Autowired
    AccountRepository accountRepository;

    @Test
    public void 유저_불러오기_테스트()   {
        Account account = createAccount();
        webTestClient.post()
                .uri(uri)
                .body(Mono.just(account), Account.class)
                .exchange()
                .expectStatus()
                .isCreated();

        webTestClient.get()
                .uri(uri + "/" + account.getId())
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void 유저_저장_테스트() {
        Account account = createAccount();

        webTestClient.post()
                .uri(uri)
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

        webTestClient.post()
                .uri(uri)
                .body(accountMono, Account.class)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.put()
                .uri(uri + "/" + account.getId())
                .body(accountMono, Account.class)
                .exchange()
                .expectStatus().isOk();

        Mono<Account> byId = accountRepository.findById(account.getId());

        StepVerifier.create(byId)
                .assertNext(i -> assertThat(account).isNotNull())
                .verifyComplete();
    }

    @Test
    public void 유저_삭제_테스트() {
        Account account = createAccount();

        Mono<Account> accountMono = Mono.justOrEmpty(account);

        webTestClient.post()
                .uri(uri)
                .body(accountMono, Account.class)
                .exchange()
                .expectStatus().isCreated();


        webTestClient.delete()
                .uri(uri + "/" + account.getId())
                .exchange()
                .expectStatus()
                .isOk();
    }

    private Account createAccount() {
        return Account.builder()
                .id(UUID.randomUUID().toString())
                .username("username-test")
                .nickname("nickname-test")
                .email("email-test")
                .password("password-test")
                .birthDate(Date.from(Instant.now()))
                .createdDate(LocalDateTime.now())
                .build();
    }

}
