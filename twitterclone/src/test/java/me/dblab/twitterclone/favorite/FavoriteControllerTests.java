package me.dblab.twitterclone.favorite;

import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountDto;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.account.AccountService;
import me.dblab.twitterclone.common.BaseControllerTest;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import me.dblab.twitterclone.tweet.Tweet;
import me.dblab.twitterclone.tweet.TweetDto;
import me.dblab.twitterclone.tweet.TweetRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.stream.IntStream;

import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
public class FavoriteControllerTests extends BaseControllerTest {

    @Autowired
    FavoriteRepository favoriteRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    FavoriteController favoriteController;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ModelMapper modelMapper;

    private final String tweetUrl = "/api/tweets";
    private final String favoriteUrl = "/api/tweet/favorites";
    private String jwt;
    private String jwt2;
    Tweet tweet;
    String accountUrl = "/api/users";
    Mono<Tweet> byAccountId;

    @BeforeEach
    public void setUp() throws Exception {

        accountRepository.deleteAll().then(favoriteRepository.deleteAll()).subscribe();

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

        //----------------------------------유저 생성 완료 -----------------------------------

        TweetDto tweetDto = tweetBuilder();

        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweetDto), Tweet.class)
                .exchange()
                .expectStatus()
                .isCreated();

        byAccountId = tweetRepository.findByAuthorEmail(currentAccount());
        tweet = byAccountId.block();

        StepVerifier.create(byAccountId)
                .assertNext(tweetObj -> {
                    then(tweetObj.getContent()).isEqualTo("경성대학교 C동 528호");
                    then(tweetObj.getAuthorEmail()).isEqualTo(appProperties.getTestEmail());
                })
                .verifyComplete();


        //----------------------------------트윗 생성 완료 -----------------------------------

        SecurityContextHolder.getContext().setAuthentication(null);

    }

    @Test
    @DisplayName("좋아요 테스트")
    public void save_favorite() throws Exception {

        AccountDto anotherAccount = createAnotherAccountDto();

        webTestClient.post()
                .uri(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(anotherAccount), Account.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Account> byEmail2 = accountRepository.findByEmail(anotherAccount.getEmail());

        StepVerifier.create(byEmail2)
                .assertNext(acc -> {
                    then(acc.getUsername()).isEqualTo(anotherAccount.getUsername());
                    then(acc.getNickname()).isEqualTo(anotherAccount.getNickname());
                    then(acc.getEmail()).isEqualTo(anotherAccount.getEmail());
                }).verifyComplete();

        jwt2 = "Bearer " + tokenProvider.generateToken(byEmail2.block());

        log.info("유저 2 생성 완료");
        log.info("유저 2 토큰 : " + jwt2);

        //----------------------------------유저2 생성 완료 -----------------------------------

        log.info(tokenProvider.getUsernameFromToken(jwt.replace("Bearer ", "")));
        log.info(tokenProvider.getUsernameFromToken(jwt2.replace("Bearer ", "")));

        webTestClient.post()
                .uri(favoriteUrl + "/" + tweet.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt2)
                .exchange()     // request 요청 & response 반환
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("accountEmail").value(Matchers.equalTo(anotherAccount.getEmail()))
                .jsonPath("tweetId").value(Matchers.equalTo(tweet.getId()));

        Mono<Favorite> byId = favoriteRepository.findByTweetId(tweet.getId());
        Favorite favorited = byId.block();

        log.info("좋아요가 눌러진 트윗 Id : " + favorited.getTweetId());
        log.info("좋아요 누른 유저의 이메일 : " + favorited.getAccountEmail());

        StepVerifier.create(byId)
                .assertNext(favorite -> {
                    then(favorite.getAccountEmail()).isEqualTo(anotherAccount.getEmail());
                    then(favorite.getTweetId()).isEqualTo(tweet.getId());
                })
                .verifyComplete();

        log.info("마지막 검증 완료");

    }

    @Test
    @DisplayName("300명의 유저 생성 후 좋아요 테스트")
    public void save_300_user_and_favorites()  {

        IntStream.rangeClosed(1, 30).forEach(i -> {

            AccountDto accountDto = createAccountDto(i);

            webTestClient.post()
                    .uri(accountUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(accountDto), Account.class)
                    .exchange()
                    .expectStatus()
                    .isCreated();

            Mono<Account> byEmail = accountRepository.findByEmail(createEmail(i));
            Account account = byEmail.block();

            StepVerifier.create(byEmail)
                    .assertNext(acc -> {
                        then(acc.getUsername()).isEqualTo(appProperties.getTestUsername());
                        then(acc.getEmail()).isEqualTo(createEmail(i));
                    }).verifyComplete();

            jwt2 = "Bearer " + tokenProvider.generateToken(account);

            SecurityContextHolder.getContext().setAuthentication(null);

            // -------------------------------유저 생성 -----------------------------------

            webTestClient.post()
                    .uri(favoriteUrl + "/" + tweet.getId())
                    .header(HttpHeaders.AUTHORIZATION, jwt2)
                    .exchange()     // request 요청 & response 반환
                    .expectStatus()
                    .isCreated()
                    .expectBody()
                    .jsonPath("accountEmail").value(Matchers.equalTo(account.getEmail()))
                    .jsonPath("tweetId").value(Matchers.equalTo(tweet.getId()));

            // ------------------------------- 좋아요 -----------------------------------

            Mono<Favorite> byId = favoriteRepository.findByAccountEmail(account.getEmail());
            Favorite favorited = byId.block();

            try {
                byAccountId = tweetRepository.findByAuthorEmail(currentAccount());
            } catch (Exception e) {
                e.printStackTrace();
            }
            tweet = byAccountId.block();

            log.info("좋아요가 눌러진 트윗 Id : " + favorited.getTweetId());
            log.info("좋아요 누른 유저의 이메일 : " + favorited.getAccountEmail());
            log.info(String.valueOf(tweet.getCountLike()));

            StepVerifier.create(byId)
                    .assertNext(favorite -> {
                        then(favorite.getAccountEmail()).isEqualTo(account.getEmail());
                        then(favorite.getTweetId()).isEqualTo(tweet.getId());
                    })
                    .verifyComplete();
        });

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

    private String createEmail(int i)   {
        return "test" + i + "@gmail.com";
    }

    private AccountDto createAccountDto(int i) {
        return AccountDto.builder()
                .username(appProperties.getTestUsername())
                .password(appProperties.getTestPassword())
                .email(createEmail(i))
                .nickname(appProperties.getTestNickname())
                .build();
    }

}
