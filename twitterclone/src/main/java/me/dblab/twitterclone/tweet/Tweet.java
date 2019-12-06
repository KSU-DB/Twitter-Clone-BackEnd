package me.dblab.twitterclone.tweet;

import lombok.*;
import me.dblab.twitterclone.account.Account;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Document
public class Tweet {

    @Id
    private String id;

    private String content;

    private LocalDateTime createdDate;

    private Account account;
}
