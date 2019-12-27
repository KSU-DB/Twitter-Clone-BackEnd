package me.dblab.twitterclone.tweet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SpringExtension.class)
class TweetTest {

    @Test
    void tweetBuilder() {
        Tweet tweet = Tweet.builder()
                .id(UUID.randomUUID().toString())
                .content("test content")
                .createdDate(LocalDateTime.now())
                .build();

        then(tweet).isNotNull();
        then(tweet.getContent()).isEqualTo("test content")
                .isNotNull();
    }

}