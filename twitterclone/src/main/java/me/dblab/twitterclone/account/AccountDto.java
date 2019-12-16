package me.dblab.twitterclone.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class AccountDto {

    private String username;
    private String nickname;
    private String password;
    private String email;
    private Date birthDate;
    private LocalDateTime createdDate;
    private List<Role> roles;

}