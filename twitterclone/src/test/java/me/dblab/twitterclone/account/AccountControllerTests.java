package me.dblab.twitterclone.account;

import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.common.BaseControllerTest;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
public class AccountControllerTests extends BaseControllerTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper modelMapper;

    private final String accountUrl = "/api/users";
    private final String BEARER = "Bearer ";
    private String jwt;

    private Mono<Account> byEmail;
    private AccountDto accountDto;

    @BeforeEach
    @DisplayName("유저 생성")
    void setUp() {
        accountRepository.deleteAll().subscribe();
        accountDto = createAccountDto();

        webTestClient.post()
                .uri(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                    .isCreated()
                .expectBody()
                    .jsonPath("password")
                    .doesNotExist();

        byEmail = accountRepository.findByEmail(appProperties.getTestEmail());

        StepVerifier.create(byEmail)
                .assertNext(acc -> {
                    then(acc.getUsername()).isEqualTo(appProperties.getTestUsername());
                    then(acc.getNickname()).isEqualTo(appProperties.getTestNickname());
                }).verifyComplete();

        jwt = BEARER + tokenProvider.generateToken(byEmail.block());
    }

    @Test
    @DisplayName("유저 불러오기 테스트")
    void get_user_test() {
        Account account = byEmail.block();

        webTestClient.get()
                .uri(accountUrl + "/" + account.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("유저 저장 테스트")
    void save_user_test() {
        StepVerifier.create(byEmail)
                .assertNext(account -> {
                    then(account.getUsername()).isEqualTo(appProperties.getTestUsername());
                    then(account.getNickname()).isEqualTo(appProperties.getTestNickname());
                    then(passwordEncoder.matches(appProperties.getTestPassword(), passwordEncoder.encode(account.getPassword())));
                    then(account.getEmail()).isEqualTo(appProperties.getTestEmail());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("유저 중복 테스트")
    void duplicated_user_test()   {

        webTestClient.post()
                .uri(accountUrl)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("유저 불러오기 테스트")
    void load_user_test() {
        Account account = byEmail.block();

        webTestClient.get()
                .uri(accountUrl + "/" + account.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("유저 수정 테스트")
    void update_user_test() {
        Account account = byEmail.block();
        AccountDto updatedAccount = updateAccountDto();

        webTestClient.put()
                .uri(accountUrl + "/" + account.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(updatedAccount), AccountDto.class)
                .exchange()
                .expectStatus()
                    .isOk()
                .expectBody()
                    .jsonPath("password").doesNotExist();

        Mono<Account> byId = accountRepository.findById(account.getId());
        Account modifiedAccount = byId.block();

        StepVerifier.create(byId)
                .assertNext(i -> {
                    then(modifiedAccount).isNotNull();
                    then(modifiedAccount.getId()).isEqualTo(account.getId());
                    then(modifiedAccount.getEmail()).isEqualTo(updatedAccount.getEmail());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("잘못된 유저 수정 테스트")
    void update_none_user_test() {
        AccountDto updatedAccount = updateAccountDto();

        webTestClient.put()
                .uri(accountUrl + "/" + UUID.randomUUID().toString())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Mono.just(updatedAccount), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();

    }

    @Test
    @DisplayName("유저 삭제 테스트")
    void delete_user_test()  {
        Account account = byEmail.block();

        webTestClient.delete()
                .uri(accountUrl + "/" + account.getId())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("존재하지않는 유저 삭제 테스트")
    void delete_invalid_user_test() throws Exception {
        webTestClient.delete()
                .uri(accountUrl + "/" + UUID.randomUUID().toString())
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("다른 유저를 삭제할 경우")
    void delete_another_user_test()  {
        AccountDto anotherAccountDto = createAnotherAccountDto();

        webTestClient.post()
                .uri(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(anotherAccountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Account> anotherByEmail = accountRepository.findByEmail(anotherAccountDto.getEmail());

        StepVerifier.create(anotherByEmail)
                .assertNext(acc -> {
                    then(acc.getUsername()).isEqualTo(anotherAccountDto.getUsername());
                    then(acc.getNickname()).isEqualTo(anotherAccountDto.getNickname());
                }).verifyComplete();

        String anotherJwt = BEARER + tokenProvider.generateToken(anotherByEmail.block());

        // ----------------------------------- Another User 생성 -------------------------------------

        Account account = byEmail.block();

        webTestClient.delete()
                .uri(accountUrl + "/" + account.getId())
                .header(HttpHeaders.AUTHORIZATION, anotherJwt)
                .exchange()
                .expectStatus()
                .isBadRequest();

    }

    @Test
    @DisplayName("로그인 정상 작동 테스트")
    void login_test() {
        //로그인
        webTestClient.post()
                .uri(accountUrl + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("token").exists();
    }

    @Test
    @DisplayName("저장되지 않은 이메일로 로그인 요청할 때")
    void login_test_not_saved_email_400() {
        accountDto.setEmail("coffeetank@gmail.com");
        //로그인
        webTestClient.post()
                .uri(accountUrl + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("로그인 시 비밀번호가 일치하지 않을 때")
    void login_test_not_matches_password_400() {
        accountDto.setPassword("americano");
        //로그인
        webTestClient.post()
                .uri(accountUrl + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(accountDto), AccountDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private AccountDto updateAccountDto() {
        return AccountDto.builder()
                .username("modified" + appProperties.getTestUsername())
                .nickname("modified" + appProperties.getTestNickname())
                .password("modified" + appProperties.getTestPassword())
                .email("modified" + appProperties.getTestEmail())
                .build();
    }
}