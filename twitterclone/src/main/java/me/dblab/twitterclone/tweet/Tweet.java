package me.dblab.twitterclone.tweet;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Document
public class Tweet {

    @Id
    private String id;

    private String content;

    private LocalDateTime createdDate;

    private String authorEmail;

    private Integer countLike = 0;
    @Nullable
    private Set<String> hashTag;
}
