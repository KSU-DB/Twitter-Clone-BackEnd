package me.dblab.twitterclone.tweet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SpringExtension.class)
public class TweetTest {

    @Test
    public void tweetBuilder() {
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