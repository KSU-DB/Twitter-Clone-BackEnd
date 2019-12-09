package me.dblab.twitterclone.tweet;

import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountDto;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.common.BaseControllerTest;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.BDDAssertions.then;

public class TweetControllerTest extends BaseControllerTest {

    @Autowired
    TweetController tweetController;

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    TokenProvider tokenProvider;

    private final String tweetUrl = "/api/tweets";
    private String jwt;

    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        AccountDto account = createAccountDto();

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

        jwt = "Bearer " + tokenProvider.generateToken(byEmail.block());
    }

    @Test
    @DisplayName("정상적으로 트윗을 생성")
    public void saveTweet() throws Exception {
        TweetDto tweetDto = tweetBuilder();

        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweetDto), Tweet.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Flux<Tweet> allByAccount = tweetRepository.findAllByAccount(currentAccount());

        StepVerifier.create(allByAccount)
                .assertNext(tweetObj -> {
                    then(tweetObj.getContent()).isEqualTo("경성대학교 C동 528호");
                    then(tweetObj.getAccount().getEmail()).isEqualTo(appProperties.getTestEmail());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("정상적으로 트윗을 삭제")
    public void deleteTweet() throws Exception {
        TweetDto tweetDto = tweetBuilder();
        createTweet(tweetDto);

        Flux<Tweet> allByAccount = tweetRepository.findAllByAccount(currentAccount());
        allByAccount.doOnNext(tweet -> webTestClient.delete()
                    .uri(tweetUrl + "/" + tweet.getId())
                    .header(HttpHeaders.AUTHORIZATION, jwt)
                    .exchange()
                    .expectStatus()
                    .isOk()
        ).subscribe(tweet -> StepVerifier.create(tweetRepository.findById(tweet.getId()))
                .verifyComplete());

    }

    @Test
    @DisplayName("정상적으로 하나의 트윗을 불러오기")
    public void getTweet() throws Exception {
        TweetDto tweetDto = tweetBuilder();
        createTweet(tweetDto);

        Flux<Tweet> allByAccount = tweetRepository.findAllByAccount(currentAccount());
        allByAccount.doOnNext(tweet -> webTestClient.get()
                .uri(tweetUrl + "/" + tweet.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("content", tweet.getContent()))
                .subscribe();
    }

    @Test
    @DisplayName("정상적으로 트윗을 수정")
    public void updateTweet() throws Exception {
        TweetDto tweetDto = tweetBuilder();

        createTweet(tweetDto);

        String update_content = "update content";
        Flux<Tweet> allByAccount = tweetRepository.findAllByAccount(currentAccount());

        allByAccount.doOnNext(tweet -> {
            tweetDto.setContent(update_content);
            webTestClient.put()
                    .uri(tweetUrl + "/" + tweet.getId())
                    .header(HttpHeaders.AUTHORIZATION, jwt)
                    .body(Mono.just(tweetDto), Tweet.class)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("content", update_content);
        }).subscribe(tweet -> StepVerifier.create(tweetRepository.findById(tweet.getId()))
                                .assertNext(tweet1 -> {
                                    then(tweet1.getId()).isEqualTo(tweet.getId());
                                    then(tweet1.getContent()).isEqualTo(update_content);
                                }).verifyComplete());

    }

    private Account currentAccount() throws Exception {
        Mono<Account> byEmail = accountRepository.findByEmail(tokenProvider.getUsernameFromToken(jwt.replace("Bearer ", "")));
        return byEmail.block();
    }

    private void createTweet(TweetDto tweetDto) {
        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweetDto), Tweet.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());

        StepVerifier.create(tweetRepository.findAllByAccount(byEmail.block()))
                .assertNext(tweetObj -> then(tweetObj.getContent()).isEqualTo("경성대학교 C동 528호"))
                .verifyComplete();
    }

    private TweetDto tweetBuilder() {
        TweetDto tweetDto = new TweetDto();
        tweetDto.setContent("경성대학교 C동 528호");
        return tweetDto;
    }
}