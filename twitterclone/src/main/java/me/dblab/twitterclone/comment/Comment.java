package me.dblab.twitterclone.comment;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Document
public class Comment {

    @Id
    private String id;

    private String content;

    private LocalDateTime createdAt;

    private String authorEmail;

    private String tweetId;
}
