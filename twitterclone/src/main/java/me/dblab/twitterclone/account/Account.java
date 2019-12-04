package me.dblab.twitterclone.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import me.dblab.twitterclone.tweet.Tweet;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
    private List<Role> roles;

    public void update(Account account) {
        this.username = account.getUsername();
        this.nickname = account.getNickname();
        this.password = account.getPassword();
        this.email = account.getEmail();
    }
}
