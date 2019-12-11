package me.dblab.twitterclone.favorite;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Document @Builder
public class Favorite {

    @Id
    private String id;
    private String accountEmail;
    private String tweetId;

}
