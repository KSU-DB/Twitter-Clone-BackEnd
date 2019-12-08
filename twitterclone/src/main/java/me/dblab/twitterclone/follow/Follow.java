package me.dblab.twitterclone.follow;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Document
public class Follow {

    @Id
    private String id;

    private String followingEmail;

    private String followerEmail;
}
