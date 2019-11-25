package me.dblab.twitterclone.tweet;

import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class TweetTest {

    @Test
    public void tweetBuilder() {
        Tweet tweet = Tweet.builder()
                .id(UUID.randomUUID().toString())
                .content("test content")
                .createdDate(LocalDateTime.now())
                .build();

        assertThat(tweet).isNotNull();
        assertThat(tweet.getContent()).isEqualTo("test content");
    }
}