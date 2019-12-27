package me.dblab.twitterclone.comment;

import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountDto;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.account.AccountService;
import me.dblab.twitterclone.common.BaseControllerTest;
import me.dblab.twitterclone.config.jwt.TokenProvider;
import me.dblab.twitterclone.tweet.Tweet;
import me.dblab.twitterclone.tweet.TweetDto;
import me.dblab.twitterclone.tweet.TweetRepository;
import me.dblab.twitterclone.tweet.TweetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

class CommentControllerTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TweetService tweetService;

    @Autowired
    TweetRepository tweetRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    CommentRepository commentRepository;

    private final String commenturl = "/api/comments/";

    @BeforeEach
    @DisplayName("유저 생성, 트윗 생성")
    public void setUp() {
        accountRepository.deleteAll().subscribe();
        tweetRepository.deleteAll().subscribe();
        commentRepository.deleteAll().subscribe();

        //유저 생성 & 등록
        webTestClient.post()
                .uri("/api/users")
                .body(Mono.just(createAccountDto()), AccountDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());
        StepVerifier.create(byEmail)
                .assertNext(account -> {
                    then(account).isNotNull();
                    then(account.getEmail()).isEqualTo(appProperties.getTestEmail());
                }).verifyComplete();

        //트윗 생성 & 등록
        TweetDto tweetDto = new TweetDto();
        tweetDto.setContent("트윗입니다.");

        webTestClient.post()
                .uri("/api/tweets")
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .body(Mono.just(tweetDto), TweetDto.class)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("content", "트윗입니다.");

        //검증
        Flux<Tweet> allByAuthorEmail = tweetRepository.findAllByAuthorEmail(Objects.requireNonNull(byEmail.block()).getEmail());
        StepVerifier.create(allByAuthorEmail)
                .assertNext(tweet -> {
                    then(tweet).isNotNull();
                    then(tweet.getContent()).isEqualTo("트윗입니다.");
                }).verifyComplete();
    }

    @Test
    @DisplayName("정상적으로 댓글을 생성")
    void saveComment() throws Exception {
        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());
        Flux<Tweet> allByAuthorEmail = tweetRepository.findAllByAuthorEmail(Objects.requireNonNull(byEmail.block()).getEmail());
        Tweet tweet = allByAuthorEmail.blockFirst();

        //댓글 생성 & 등록
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("댓글입니다.");

        webTestClient.post()
                .uri(commenturl + tweet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .body(Mono.just(commentDto), CommentDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        StepVerifier.create(commentRepository.findAllByTweetId(tweet.getId()))
                .assertNext(comment -> {
                    then(comment).isNotNull();
                    then(comment.getContent()).isEqualTo("댓글입니다.");
                }).verifyComplete();
    }

    @DisplayName("댓글 생성 시 댓글의 내용이 없어, Bad Request를 반환하는 테스트")
    @ParameterizedTest(name = "{displayName}{index}")
    @ValueSource(strings = {"", "          "})
    void saveComment_bad_input_400_bad_request(String content) {
        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());
        Flux<Tweet> allByAuthorEmail = tweetRepository.findAllByAuthorEmail(Objects.requireNonNull(byEmail.block()).getEmail());
        Tweet tweet = allByAuthorEmail.blockFirst();

        CommentDto commentDto = new CommentDto();
        commentDto.setContent(content);

        webTestClient.post()
                .uri(commenturl + tweet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .body(Mono.just(commentDto), CommentDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("정상적으로 댓글을 수정")
    void updateComment() {
        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());
        Flux<Tweet> allByAuthorEmail = tweetRepository.findAllByAuthorEmail(Objects.requireNonNull(byEmail.block()).getEmail());
        Tweet tweet = allByAuthorEmail.blockFirst();

        //댓글 생성 & 등록
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("댓글입니다.");

        webTestClient.post()
                .uri(commenturl + tweet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .body(Mono.just(commentDto), CommentDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Flux<Comment> allByTweetId = commentRepository.findAllByTweetId(tweet.getId());
        StepVerifier.create(allByTweetId)
                .assertNext(comment -> {
                    then(comment).isNotNull();
                    then(comment.getContent()).isEqualTo("댓글입니다.");
                }).verifyComplete();

        //댓글 수정
        Comment comment = allByTweetId.blockFirst();
        commentDto.setContent("수정할 댓글입니다.");

        webTestClient.put()
                .uri(commenturl + "/" + comment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .body(Mono.just(commentDto), CommentDto.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("content", "수정할 댓글입니다.");
    }

    @DisplayName("댓글 수정 시 댓글의 내용이 없어, Bad Request를 반환하는 테스트")
    @ParameterizedTest(name = "{displayName}{index}")
    @ValueSource(strings = {"", "          "})
    void updateComment_empty_input_400_bad_request(String content) {
        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());
        Flux<Tweet> allByAuthorEmail = tweetRepository.findAllByAuthorEmail(Objects.requireNonNull(byEmail.block()).getEmail());
        Tweet tweet = allByAuthorEmail.blockFirst();

        //댓글 생성 & 등록
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("댓글입니다.");

        webTestClient.post()
                .uri(commenturl + tweet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .body(Mono.just(commentDto), CommentDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Flux<Comment> allByTweetId = commentRepository.findAllByTweetId(tweet.getId());
        StepVerifier.create(allByTweetId)
                .assertNext(comment -> {
                    then(comment).isNotNull();
                    then(comment.getContent()).isEqualTo("댓글입니다.");
                }).verifyComplete();

        //댓글 수정
        Comment comment = allByTweetId.blockFirst();
        commentDto.setContent(content);

        webTestClient.put()
                .uri(commenturl + "/" + comment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .body(Mono.just(commentDto), CommentDto.class)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("정상적으로 댓글을 삭제")
    void deleteComment() {
        //댓글 생성
        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());
        Flux<Tweet> allByAuthorEmail = tweetRepository.findAllByAuthorEmail(Objects.requireNonNull(byEmail.block()).getEmail());
        Tweet tweet = allByAuthorEmail.blockFirst();

        //댓글 생성 & 등록
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("댓글입니다.");

        webTestClient.post()
                .uri(commenturl + tweet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .body(Mono.just(commentDto), CommentDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Flux<Comment> allByTweetId = commentRepository.findAllByTweetId(tweet.getId());
        StepVerifier.create(allByTweetId)
                .assertNext(comment -> {
                    then(comment).isNotNull();
                    then(comment.getContent()).isEqualTo("댓글입니다.");
                }).verifyComplete();

        //댓글 삭제
        Comment comment = allByTweetId.blockFirst();

        webTestClient.delete()
                .uri(commenturl + "/" + comment.getId())
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @DisplayName("댓글 삭제 시, 데이터베이스에 저장되어 있지 않은 댓글의 삭제를 요청할때, Bad Request를 반환하는 테스트")
    void delete_bad_request() {
        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());
        Flux<Tweet> allByAuthorEmail = tweetRepository.findAllByAuthorEmail(Objects.requireNonNull(byEmail.block()).getEmail());
        Tweet tweet = allByAuthorEmail.blockFirst();

        //댓글 생성 & 등록
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("댓글입니다.");

        webTestClient.post()
                .uri(commenturl + tweet.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .body(Mono.just(commentDto), CommentDto.class)
                .exchange()
                .expectStatus()
                .isCreated();

        Flux<Comment> allByTweetId = commentRepository.findAllByTweetId(tweet.getId());
        StepVerifier.create(allByTweetId)
                .assertNext(comment -> {
                    then(comment).isNotNull();
                    then(comment.getContent()).isEqualTo("댓글입니다.");
                }).verifyComplete();

        //댓글 삭제
        webTestClient.delete()
                .uri(commenturl + "/" + UUID.randomUUID().toString())
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @DisplayName("정상적으로 댓글 목록 불러오기")
    void getCommentList() {
        Mono<Account> byEmail = accountRepository.findByEmail(appProperties.getTestEmail());
        Flux<Tweet> allByAuthorEmail = tweetRepository.findAllByAuthorEmail(Objects.requireNonNull(byEmail.block()).getEmail());
        Tweet tweet = allByAuthorEmail.blockFirst();

        //댓글 10개 생성 & 등록
        IntStream.rangeClosed(1, 10)
                .forEach(index -> {
                    CommentDto commentDto = new CommentDto();
                    commentDto.setContent("경성대학교 투썸플레이스에서 댓글 담" + index);
                    webTestClient.post()
                            .uri(commenturl + "/" + tweet.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                            .body(Mono.just(commentDto), CommentDto.class)
                            .exchange()
                            .expectStatus()
                            .isCreated()
                            .expectBody()
                            .jsonPath("content", "경성대학교 투썸플레이스에서 댓글 담" + index);
                });

        //댓글 불러오기
        webTestClient.get()
                .uri(commenturl + "/" + tweet.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, createJwt(byEmail.block()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("[*].content").exists();
    }

    String createJwt(Account account) {
        return "Bearer " + tokenProvider.generateToken(account);
    }

}