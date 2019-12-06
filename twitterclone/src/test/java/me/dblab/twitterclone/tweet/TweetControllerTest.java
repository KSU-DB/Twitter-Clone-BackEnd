package me.dblab.twitterclone.tweet;

import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.account.Role;
import me.dblab.twitterclone.common.AppProperties;
import me.dblab.twitterclone.common.BaseControllerTest;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;

public class TweetControllerTest extends BaseControllerTest {

    @Autowired
    TweetController tweetController;

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    AppProperties appProperties;

    @Autowired
    TokenProvider tokenProvider;

    private final String tweetUrl = "/api/tweets";
    private String jwt;

    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        Account account = Account.builder()
                .email(appProperties.getTestEmail())
                .username(appProperties.getTestUsername())
                .password(appProperties.getTestPassword())
                .nickname(appProperties.getTestNickname())
                .roles(Collections.singletonList(Role.USER))
                .build();

        String accountUrl = "/api/users";
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

        jwt = "Bearer " + tokenProvider.generateToken(account);
    }

    @Test
    @DisplayName("정상적으로 트윗을 생성")
    public void saveTweet() {
        Tweet tweet = tweetBuilder();

        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweet), Tweet.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Tweet> byId = tweetRepository.findById(tweet.getId());

        StepVerifier.create(byId)
                .assertNext(tweetObj -> {
                    then(tweetObj.getContent()).isEqualTo("test content");
                    then(tweetObj.getAccount().getEmail()).isEqualTo(appProperties.getTestEmail());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("정상적으로 트윗을 삭제")
    public void deleteTweet() {
        Tweet tweet = tweetBuilder();
        createTweet(tweet);

        webTestClient.delete()
                .uri(tweetUrl + "/" + tweet.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk();

        StepVerifier.create(tweetRepository.findById(tweet.getId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("정상적으로 하나의 트윗을 불러오기")
    public void getTweet() {
        Tweet tweet = tweetBuilder();
        createTweet(tweet);

        webTestClient.get()
                .uri(tweetUrl + "/" + tweet.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("content", tweet.getContent());
    }

    @Test
    @DisplayName("정상적으로 트윗을 수정")
    public void updateTweet() {
        Tweet tweet = tweetBuilder();

        createTweet(tweet);

        String update_content = "update content";

        tweet.setContent(update_content);
        webTestClient.put()
                .uri(tweetUrl + "/" + tweet.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweet), Tweet.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("content", update_content);

        StepVerifier.create(tweetRepository.findById(tweet.getId()))
                .assertNext(tweet1 -> {
                    then(tweet1.getId()).isEqualTo(tweet.getId());
                    then(tweet1.getContent()).isEqualTo(update_content);
                }).verifyComplete();
    }

    private void createTweet(Tweet tweet) {
        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweet), Tweet.class)
                .exchange()
                .expectStatus()
                .isCreated();

        StepVerifier.create(tweetRepository.findById(tweet.getId()))
                .assertNext(tweetObj -> then(tweetObj.getContent()).isEqualTo("test content"))
                .verifyComplete();
    }

    private Tweet tweetBuilder() {
        return Tweet.builder().id(UUID.randomUUID().toString()).content("test content").createdDate(LocalDateTime.now()).build();
    }
}