package me.dblab.twitterclone.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AccountServiceTests {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Test
    public void 유저_디테일즈_테스트()    {
        Account account = Account.builder()
                .id(UUID.randomUUID().toString())
                .username("username-test")
                .nickname("nickname-test")
                .email("email-test")
                .password("password-test")
                .birthDate(Date.from(Instant.now()))
                .createdDate(LocalDateTime.now())
                .build();

        Mono.just(account).flatMap(accountRepository::save).subscribe();

    }
}
