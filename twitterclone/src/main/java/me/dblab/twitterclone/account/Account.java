package me.dblab.twitterclone.account;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder @Document
public class Account {

    @Id
    private String id;
    private String username;
    private String nickname;
    private String password;
    private String email;
    private Date birthDate;
    private LocalDateTime createdDate;

}
