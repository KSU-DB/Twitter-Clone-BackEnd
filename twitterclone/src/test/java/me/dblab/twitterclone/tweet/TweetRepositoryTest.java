package me.dblab.twitterclone.tweet;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TweetRepositoryTest {

    @Autowired
    TweetRepository tweetRepository;

    @BeforeEach
    public void setUp() {
        tweetRepository.deleteAll().subscribe();
    }

    @Test
    public void createTweetTest() {
        Tweet tweet = Tweet.builder().id(UUID.randomUUID().toString()).content("test content").createdDate(LocalDateTime.now()).build();

        //save
        Mono.justOrEmpty(tweet)
                .flatMap(tweetRepository::save)
                .subscribe(x -> log.info("saved :" + x.toString()));

        Mono<Tweet> byId = tweetRepository.findById(tweet.getId());
        //verify
        StepVerifier.create(byId)
                .assertNext(index -> assertThat(index.getContent()).isEqualTo("test content"))
                .verifyComplete();
    }

    @Test
    public void createTweetTest_notCreated() {
        Tweet tweet = Tweet.builder().id(UUID.randomUUID().toString()).content("test content").createdDate(LocalDateTime.now()).build();

        //save
        Mono.justOrEmpty(tweet)
                .flatMap(tweetRepository::save);

        Mono<Tweet> byId = tweetRepository.findById(tweet.getId());
        //verify
        StepVerifier.create(byId)
                .verifyComplete();
    }
}