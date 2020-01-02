package me.dblab.twitterclone.explore;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Setter @Getter
@AllArgsConstructor @NoArgsConstructor
@Builder @Document
public class Explore {

    @Id
    private String id;
    private String keyword;
    private boolean isSaved;
    private LocalDateTime searchedAt;
    private String accountEmail;

}
