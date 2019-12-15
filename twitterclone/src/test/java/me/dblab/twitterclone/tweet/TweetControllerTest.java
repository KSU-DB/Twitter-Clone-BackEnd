package me.dblab.twitterclone.tweet;

import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountDto;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.account.AccountService;
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

import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
public class TweetControllerTest extends BaseControllerTest {

    @Autowired
    TweetController tweetController;

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    TweetService tweetService;

    @Autowired
    AccountService accountService;

    @Autowired
    TokenProvider tokenProvider;

    private final String tweetUrl = "/api/tweets";
    private String jwt;

    @Autowired
    AccountRepository accountRepository;

    private final String accountUrl = "/api/users";

    @BeforeEach
    void setUp() {
        tweetRepository.deleteAll().subscribe();
        accountRepository.deleteAll().subscribe();
        AccountDto account = createAccountDto();

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

        Flux<Tweet> allByAccount = tweetRepository.findAllByAuthorEmail(currentAccount());

        StepVerifier.create(allByAccount)
                .assertNext(tweetObj -> {
                    then(tweetObj.getContent()).isEqualTo("경성대학교 C동 528호");
                    then(tweetObj.getAuthorEmail()).isEqualTo(appProperties.getTestEmail());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("정상적으로 트윗을 삭제")
    public void deleteTweet() throws Exception {
        TweetDto tweetDto = tweetBuilder();
        createTweet(tweetDto);

        Flux<Tweet> allByAccount = tweetRepository.findAllByAuthorEmail(currentAccount());
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
    @DisplayName("다른 사용자의 게시물을 삭제")
    public void deleteTweet_with_bad_request() throws Exception {
        //트윗 생성
        TweetDto tweetDto = tweetBuilder();
        createTweet(tweetDto);

        //유저1의 tweet들을 불러온다.
        Flux<Tweet> allByAccount = tweetRepository.findAllByAuthorEmail(currentAccount());

        StepVerifier.create(allByAccount)
                .assertNext(tweet -> then(tweet.getAuthorEmail()).isEqualTo(appProperties.getTestEmail()))
                .verifyComplete();

        //유저2 생성
        AccountDto account = createAccountDto(2);

        //유저2 등록
        webTestClient.post()
                .uri(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(account), AccountDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        //db에서 유저2를 불러온다.
        Mono<Account> byEmail = accountRepository.findByEmail(createEmail(2));
        String jwt2 = "Bearer " + tokenProvider.generateToken(byEmail.block());

        Tweet tweet1 = allByAccount.blockLast();

        //유저1의 게시물을 유저2가 삭제 요청을 한다.
        webTestClient.delete()
                .uri(tweetUrl + "/" + tweet1.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt2)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("정상적으로 하나의 트윗을 불러오기")
    public void getTweet() throws Exception {
        TweetDto tweetDto = tweetBuilder();
        createTweet(tweetDto);

        Flux<Tweet> allByAccount = tweetRepository.findAllByAuthorEmail(currentAccount());
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
        Flux<Tweet> allByAccount = tweetRepository.findAllByAuthorEmail(currentAccount());

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

    @Test
    @DisplayName("DB에 없는 트윗의 수정을 요청했을 때")
    public void updateTweet_400_Bad_Request() throws Exception {
        //트윗 생성
        TweetDto tweetDto = tweetBuilder();
        createTweet(tweetDto);

        Flux<Tweet> allByAccount = tweetRepository.findAllByAuthorEmail(currentAccount());

        //저장되었는지 검증
        StepVerifier.create(allByAccount)
                .assertNext(tweet -> then(tweet.getContent()).isEqualTo("경성대학교 C동 528호"))
                .verifyComplete();

        Tweet tweet2 = allByAccount.blockFirst();

        //임의의 id로 요청
        webTestClient.put()
                .uri(tweetUrl + "/" + UUID.randomUUID().toString())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweetDto), Tweet.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

        //수정이 안됐는지 검증
       StepVerifier.create(tweetRepository.findById(tweet2.getId()))
                .assertNext(tweet1 -> {
                    then(tweet1.getId()).isEqualTo(tweet2.getId());
                    then(tweet1.getContent()).isEqualTo("경성대학교 C동 528호");
                }).verifyComplete();
    }

    @Test
    @DisplayName("유저가 팔로잉한 유저들의 게시물만 불러오기")
    public void getTweetList() {
        //유저 10명 생성 & 등록
        IntStream.rangeClosed(1, 10).forEach(index -> {
            AccountDto accountDto = createAccountDto(index);
            webTestClient.post()
                    .uri(accountUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(accountDto), Account.class)
                    .exchange()
                    .expectStatus()
                    .isCreated();

            StepVerifier.create(accountRepository.findByEmail(createEmail(index)))
                    .assertNext(account -> then(account.getEmail()).isEqualTo(createEmail(index)))
                    .verifyComplete();
        });

        //유저 10명에게 각각 1개의 트윗 등록
        IntStream.rangeClosed(1, 10).forEach(index -> {
            TweetDto tweetDto = new TweetDto();
            tweetDto.setContent("경성대 투썸플레이스" + index);
            try {
                createTweet(tweetDto, index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //검증
        IntStream.rangeClosed(1, 10).forEach(index ->
                StepVerifier.create(tweetRepository.findAllByAuthorEmail(createEmail(index)))
                        .assertNext(tweet -> then(tweet.getAuthorEmail()).isEqualTo(createEmail(index)))
                        .verifyComplete()
        );

        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());
        //5명만 팔로잉
        Account account = byEmail.block();
        jwt = "Bearer " + tokenProvider.generateToken(account);
        IntStream.rangeClosed(1, 5).forEach(index ->
                webTestClient.post()
                        .uri("/api/follows/" + createEmail(index))
                        .header(HttpHeaders.AUTHORIZATION, jwt)
                        .exchange()
                        .expectStatus()
                        .isCreated()
        );

        //검증
        webTestClient.get()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("[*].content").exists();

        IntStream.rangeClosed(1, 5)
                .forEach(index ->
                        StepVerifier.create(tweetRepository.findAllByAuthorEmailOrderByCreatedDateDesc(createEmail(index)))
                                .assertNext(tweet -> then(tweet.getAuthorEmail()).isEqualTo(createEmail(index)))
                                .verifyComplete()
                );
    }

    private void createTweet(TweetDto tweetDto, int index) throws Exception {
        Mono<Account> byEmail = accountRepository.findByEmail(createEmail(index));
        jwt = "Bearer " + tokenProvider.generateToken(byEmail.block());

        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweetDto), TweetDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        StepVerifier.create(tweetRepository.findAllByAuthorEmail(currentAccount()))
                .assertNext(tweetObj -> then(tweetObj).isNotNull())
                .verifyComplete();
    }

    private void createTweet(TweetDto tweetDto) throws Exception {
        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweetDto), TweetDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        StepVerifier.create(tweetRepository.findAllByAuthorEmail(currentAccount()))
                .assertNext(tweetObj -> then(tweetObj).isNotNull())
                .verifyComplete();
    }

    private String currentAccount() throws Exception {
        Mono<Account> byEmail = accountRepository.findByEmail(tokenProvider.getUsernameFromToken(jwt.replace("Bearer ", "")));
        return Objects.requireNonNull(byEmail.block()).getEmail();
    }

    private TweetDto tweetBuilder() {
        TweetDto tweetDto = new TweetDto();
        tweetDto.setContent("경성대학교 C동 528호");
        return tweetDto;
    }

    private String createEmail(int index) {
        return "test" + index + "@gmail.com";
    }

    private AccountDto createAccountDto(int index) {
        return AccountDto.builder()
                .email(createEmail(index))
                .username(appProperties.getTestUsername())
                .password(appProperties.getTestPassword())
                .nickname(appProperties.getTestNickname())
                .build();
    }
}