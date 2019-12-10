package me.dblab.twitterclone.favorite;

import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountDto;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.common.BaseControllerTest;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import me.dblab.twitterclone.tweet.Tweet;
import me.dblab.twitterclone.tweet.TweetDto;
import me.dblab.twitterclone.tweet.TweetRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
public class FavoriteControllerTests extends BaseControllerTest {

    @Autowired
    FavoriteRepository favoriteRepository;

    @Autowired
    FavoriteController favoriteController;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TweetRepository tweetRepository;

    private final String tweetUrl = "/api/tweets";
    private final String favoriteUrl = "/api/tweet/favorites";
    private String jwt;
    private String jwt2;
    Tweet tweet;
    String accountUrl = "/api/users";

    @BeforeEach
    public void setUp() throws Exception {
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

        log.info("유저 1 생성 완료");
        log.info("유저 1 토큰 : " + jwt);

        //----------------------------------유저 생성 완료 -----------------------------------

        TweetDto tweetDto = tweetBuilder();

        webTestClient.post()
                .uri(tweetUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(tweetDto), Tweet.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Tweet> byAccountId = tweetRepository.findByAccount_Email(currentAccount().getEmail());
        tweet = byAccountId.block();

        StepVerifier.create(byAccountId)
                .assertNext(tweetObj -> {
                    then(tweetObj.getContent()).isEqualTo("경성대학교 C동 528호");
                    then(tweetObj.getAccount().getEmail()).isEqualTo(appProperties.getTestEmail());
                })
                .verifyComplete();


        log.info("트윗 생성 완료");
        log.info("트윗 id : " + tweet.getId());

        //----------------------------------트윗 생성 완료 -----------------------------------

        // 각각의 요청마다 SecurityFilter를 동작시키기 위해
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

    private Account currentAccount() throws Exception {
        Mono<Account> byEmail = accountRepository.findByEmail(tokenProvider.getUsernameFromToken(jwt.replace("Bearer ", "")));
        return byEmail.block();
    }

    private Account currentAnotherAccount() throws Exception {
        Mono<Account> byEmail2 = accountRepository.findByEmail(tokenProvider.getUsernameFromToken(jwt2.replace("Bearer ", "")));
        return byEmail2.block();
    }

    private TweetDto tweetBuilder() {
        TweetDto tweetDto = new TweetDto();
        tweetDto.setContent("경성대학교 C동 528호");
        return tweetDto;
    }

//    public AccountDto createAnotherAccountDto() {
//        return AccountDto.builder()
//                .email("another" + appProperties.getTestEmail())
//                .username("another" + appProperties.getTestUsername())
//                .password("another" + appProperties.getTestPassword())
//                .nickname("another" + appProperties.getTestNickname())
//                .roles(Collections.singletonList(Role.USER))
//                .build();
//    }

}
