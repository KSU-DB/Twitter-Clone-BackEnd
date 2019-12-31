package me.dblab.twitterclone.explore;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
public class ExploreControllerTests extends BaseControllerTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ExploreRepository exploreRepository;

    @Autowired
    ModelMapper modelMapper;

    private final String accountUrl = "/api/users";
    private final String tweetUrl = "/api/tweets";
    private final String exploreUrl = "/api/explores";
    private final String BEARER = "Bearer ";

    private String jwt;

    @BeforeEach
    @DisplayName("유저 생성 & 트윗 30개 생성")
    void setUp() {
        accountRepository.deleteAll().then(exploreRepository.deleteAll()).subscribe();

        AccountDto accountDto = createAccountDto();

        webTestClient.post()
                .uri(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(accountDto), Account.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());

        StepVerifier.create(byEmail)
                .assertNext(acc -> {
                    then(acc.getUsername()).isEqualTo(appProperties.getTestUsername());
                    then(acc.getNickname()).isEqualTo(appProperties.getTestNickname());
                }).verifyComplete();

        jwt = BEARER + tokenProvider.generateToken(byEmail.block());

        IntStream.rangeClosed(1, 30).forEach(i -> {
            TweetDto tweetDto = tweetBuilder(i);

            webTestClient.post()
                    .uri(tweetUrl)
                    .header(HttpHeaders.AUTHORIZATION, jwt)
                    .body(Mono.just(tweetDto), Tweet.class)
                    .exchange()
                    .expectStatus()
                    .isCreated();
        });

        IntStream.rangeClosed(1, 30).forEach(i ->
                StepVerifier.create(tweetRepository.findAllByAuthorEmail(createEmail(i)))
                        .assertNext(tw ->
                                then(tw.getContent()).isEqualTo("게시물" + i)
                        ));
    }

    @Test
    @DisplayName("특정 문자가 포함된 트윗 검색")
    void exploreTweet() throws Exception {
        ExploreDto exploreDto = exploreBuilder();

        webTestClient.post()
                .uri(exploreUrl + "/keywords")
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(exploreDto), ExploreDto.class)
                .exchange()
                .expectStatus()
                .isOk();

        Flux<Explore> exploreFlux = exploreRepository.findAllByAccountEmailOrderByKeyword(currentAccount());

        StepVerifier.create(exploreFlux)
                .assertNext(exp ->
                        then(exp.isSaved()).isEqualTo(false))
                .verifyComplete();
    }

    @Test
    @DisplayName("keyword가 null인 트윗 검색")
    void exploreTweet_null() throws Exception {
        ExploreDto exploreDto = new ExploreDto();

        webTestClient.post()
                .uri(exploreUrl + "/keywords")
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(exploreDto), ExploreDto.class)
                .exchange()
                .expectStatus()
                .isOk();

        Flux<Explore> exploreFlux = exploreRepository.findAllByAccountEmailOrderByKeyword(currentAccount());

        StepVerifier.create(exploreFlux)
                .verifyComplete();
    }

    @Test
    @DisplayName("검색어 저장")
    void save_explored_keyword_test() throws Exception {
        ExploreDto exploreDto = exploreBuilder();
        saveExplore(exploreDto);

        Flux<Explore> exploreFlux = exploreRepository.findAllByAccountEmailOrderByKeyword(currentAccount());

        StepVerifier.create(exploreFlux)
                .assertNext(exp -> {
                    then(exp.getKeyword()).isEqualTo(exploreDto.getKeyword());
                    then(exp.isSaved()).isEqualTo(true);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("저장할 검색어가 null일 시 BadRequest")
    void save_explored_keyword_validate() {
        ExploreDto exploreDto = new ExploreDto();

        webTestClient.post()
                .uri(exploreUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(exploreDto), ExploreDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("저장된 검색어 불러오기")
    void get_saved_keyword_test()   {
        ExploreDto exploreDto = exploreBuilder();
        saveExplore(exploreDto);

        webTestClient.get()
                .uri(exploreUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("저장된 키워드 삭제")
    public void delete_saved_keyword_test() throws Exception {
        ExploreDto exploreDto = exploreBuilder();
        saveExplore(exploreDto);

        Flux<Explore> exploreFlux = exploreRepository.findAllByAccountEmailOrderByKeyword(currentAccount());

        Explore explore = exploreFlux.blockLast();

        webTestClient.delete()
                .uri(exploreUrl + "/" + explore.getId())
                .header(HttpHeaders.AUTHORIZATION,jwt)
                .exchange()
                .expectStatus()
                .isOk();

        StepVerifier.create(exploreRepository.findById(explore.getId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("다른 유저의 저장된 키워드 삭제")
    public void delete_another_account_saved_keyword_test() throws Exception {
        ExploreDto exploreDto = exploreBuilder();
        saveExplore(exploreDto);

        Flux<Explore> exploreFlux = exploreRepository.findAllByAccountEmailOrderByKeyword(currentAccount());
        Explore explore = exploreFlux.blockLast();
        log.info("explore 주인 : " + explore.getAccountEmail());
        log.info("currentAccount : " + explore.getAccountEmail());
        AccountDto anotherAccountDto = createAnotherAccountDto();

        webTestClient.post()
                .uri(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(anotherAccountDto), Account.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Account> byEmail2 = accountRepository.findByEmail(anotherAccountDto.getEmail());
        Account account = byEmail2.block();
        log.info("account : " + account.getEmail());
        String jwt2 = BEARER + tokenProvider.generateToken(account);

        webTestClient.delete()
                .uri(exploreUrl + "/" + explore.getId())
                .header(HttpHeaders.AUTHORIZATION,jwt2)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("잘못된 아이디로 저장된 키워드 삭제할 시 BadRequest")
    public void delete_saved_keyword_with_invalid_id_test() throws Exception {
        ExploreDto exploreDto = exploreBuilder();
        saveExplore(exploreDto);

        webTestClient.delete()
                .uri(exploreUrl + "/" + UUID.randomUUID().toString())
                .header(HttpHeaders.AUTHORIZATION,jwt)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private String currentAccount() throws Exception {
        Mono<Account> byEmail = accountRepository.findByEmail(tokenProvider.getUsernameFromToken(jwt.replace(BEARER, "")));
        return Objects.requireNonNull(byEmail.block()).getEmail();
    }

    private String createEmail(int index) {
        return "test" + index + "@gmail.com";
    }

    private TweetDto tweetBuilder(int i) {
        TweetDto tweetDto = new TweetDto();
        tweetDto.setContent("게시물" + i);
        return tweetDto;
    }

    private ExploreDto exploreBuilder() {
        ExploreDto exploreDto = new ExploreDto();
        exploreDto.setKeyword("키워드");
        return exploreDto;
    }

    private void saveExplore(ExploreDto exploreDto) {
        webTestClient.post()
                .uri(exploreUrl)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(exploreDto), ExploreDto.class)
                .exchange()
                .expectStatus()
                .isCreated();
    }

}
