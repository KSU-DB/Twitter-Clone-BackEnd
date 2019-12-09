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
        //유저 생성
        AccountDto accountDto = createAccountDto(0);
        //유저 등록
        accountService.saveAccount(accountDto).subscribe();

        Mono<Account> byEmail = accountRepository.findByEmail(createEmail(0));
        StepVerifier.create(byEmail)
                .assertNext(e -> {
                    then(e).isNotNull();
                    then(e.getEmail()).isEqualTo(createEmail(0));
                }).verifyComplete();

        Account map = modelMapper.map(accountDto, Account.class);
        jwt = "Bearer " + tokenProvider.generateToken(map);

        //30개의 유저 생성(팔로잉할 유저)
        IntStream.rangeClosed(1, 30).forEach(index -> {
            //유저 생성
            AccountDto following = createAccountDto(index);
            //유저 등록
            accountService.saveAccount(following).subscribe();
            //검증
            StepVerifier.create(accountRepository.findByEmail(createEmail(index)))
                    .assertNext(fu -> {
                        then(fu).isNotNull();
                        then(fu.getEmail()).isEqualTo(createEmail(index));
                    }).verifyComplete();
        });
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