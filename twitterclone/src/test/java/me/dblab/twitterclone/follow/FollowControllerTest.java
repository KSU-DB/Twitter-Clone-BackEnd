package me.dblab.twitterclone.follow;

import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountDto;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.account.AccountService;
import me.dblab.twitterclone.common.BaseControllerTest;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

public class FollowControllerTest extends BaseControllerTest {

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    FollowRepository followRepository;

    private final String followUrl = "/api/follows";
    private String jwt;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll().then(followRepository.deleteAll()).subscribe();

        //31개의 유저 생성 --> 유저0이 유저1~30을 팔로잉
        IntStream.rangeClosed(0, 30).forEach(index -> {
            //유저 생성
            AccountDto following = createAccountDto(index);
            //유저 등록
            accountService.saveAccount(following).subscribe();
        });

        //검증
        IntStream.rangeClosed(0, 30).forEach(index ->
            StepVerifier.create(accountRepository.findByEmail(createEmail(index)))
                    .assertNext(account -> then(account.getEmail()).isEqualTo(createEmail(index)))
                    .verifyComplete()
        );

        //jwt 생성
        Account block = accountRepository.findByEmail(createEmail(0)).block();
        jwt = "Bearer " + tokenProvider.generateToken(block);
    }

    @Test
    @DisplayName("정상적으로 팔로잉이 동작하는 테스트")
    public void following() {
        //팔로잉할 유저 이메일을 파라미터로 넘김
        IntStream.rangeClosed(1, 30).forEach(index ->
            webTestClient.post()
                    .uri(followUrl + "/" + createEmail(index))
                    .header(HttpHeaders.AUTHORIZATION, jwt)
                    .exchange()
                    .expectStatus()
                    .isCreated()
                    .expectBody()
                    .jsonPath("followingEmail").value(Matchers.equalTo(createEmail(index)))
                    .jsonPath("followerEmail").value(Matchers.equalTo(createEmail(0)))
        );

        Flux<Follow> allByFollowerEmail = followRepository.findAllByFollowerEmail(createEmail(0));
        StepVerifier.create(allByFollowerEmail)
                .expectNextCount(30L)
                .verifyComplete();
    }

    @Test
    @DisplayName("DB에 없는 유저의 팔로잉을 요청했을 때")
    public void following_400_Bad_Request() {
        //DB에 없는 유저를 팔로잉함
        webTestClient.post()
                .uri(followUrl + "/" + createEmail(32))
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isBadRequest();

        Flux<Follow> allByFollowerEmail = followRepository.findAllByFollowerEmail(createEmail(0));
        StepVerifier.create(allByFollowerEmail)
                .verifyComplete();
    }

    @Test
    @DisplayName("정상적으로 언팔로우가 동작하는 테스트")
    public void unfollow() {
        //30개의 팔로잉 등록
        IntStream.rangeClosed(1, 30).forEach(index ->
                webTestClient.post()
                        .uri(followUrl + "/" + createEmail(index))
                        .header(HttpHeaders.AUTHORIZATION, jwt)
                        .exchange()
                        .expectStatus()
                        .isCreated()
                        .expectBody()
                        .jsonPath("followingEmail").value(Matchers.equalTo(createEmail(index)))
                        .jsonPath("followerEmail").value(Matchers.equalTo(createEmail(0)))
        );

        //5개의 계정 언팔로우
        IntStream.rangeClosed(1, 5).forEach(index -> {
            Mono<Follow> byFollowingEmail = followRepository.findByFollowingEmail(createEmail(index));
            String id = Optional.ofNullable(byFollowingEmail.block().getId()).orElseGet(() -> fail("테스트 실패!"));

            webTestClient.delete()
                .uri(followUrl + "/" + id)
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk();
        });

        Flux<Follow> allByFollowerEmail = followRepository.findAllByFollowerEmail(createEmail(0));
        StepVerifier.create(allByFollowerEmail)
                .expectNextCount(25L)
                .verifyComplete();
    }

    private String createEmail(int index) {
        return "test" + index + "@gmail.com";
    }

    private AccountDto createAccountDto(int index) {
        return AccountDto.builder()
                .username(appProperties.getTestUsername())
                .password(appProperties.getTestPassword())
                .email(createEmail(index))
                .nickname(appProperties.getTestNickname())
                .build();
    }
}