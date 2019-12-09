package me.dblab.twitterclone.tweet;

import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountDto;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.common.BaseControllerTest;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.BDDAssertions.then;

public class TweetValidatorTest extends BaseControllerTest {

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TweetRepository tweetRepository;

    private String jwt;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll().then(tweetRepository.deleteAll()).subscribe();
        String accountUrl = "/api/users";

        //유저 생성
        AccountDto account = createAccountDto();
        //유저 등록
        webTestClient.post()
                .uri(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(account), Account.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());

        StepVerifier.create(byEmail)
                .assertNext(acc -> {
                    then(acc.getUsername()).isEqualTo(appProperties.getTestUsername());
                    then(acc.getNickname()).isEqualTo(appProperties.getTestNickname());
                }).verifyComplete();

        jwt = "Bearer " + tokenProvider.generateToken(byEmail.block());
    }

    @Test
    @DisplayName("컨텐츠의 길이가 0일때")
    public void contentValidate() {
        String tweetUrl = "/api/tweets";

        //트윗 생성
        TweetDto tweetDto = new TweetDto();
        tweetDto.setContent("");

        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweetDto), TweetDto.class)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("content", "Content must be at least 1 character long or less than 255 characters long.");
    }

    @Test
    @DisplayName("컨텐츠의 길이가 255이상 일 때")
    public void contentValidate2() {
        String tweetUrl = "/api/tweets";
        String randomString = RandomStringUtils.random(256);

        then(randomString.length()).isGreaterThan(255);
        //트윗 생성
        TweetDto tweetDto = new TweetDto();
        tweetDto.setContent(randomString);

        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweetDto), TweetDto.class)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("content", "Content must be at least 1 character long or less than 255 characters long.");
    }
}