package me.dblab.twitterclone.favorite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class FavoriteTest {

    @Test
    public void favoriteBuilder()   {
        String tweetId = UUID.randomUUID().toString();
        String accountEmail = "test@gmail.com";

        Favorite favorite = Favorite.builder()
                .tweetId(tweetId)
                .accountEmail(accountEmail)
                .build();

        assertThat(favorite).isNotNull();
        assertThat(favorite.getTweetId()).isEqualTo(tweetId);
        assertThat(favorite.getAccountEmail()).isEqualTo(accountEmail);
    }

}
