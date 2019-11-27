package me.dblab.twitterclone.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

@Builder @Data
@AllArgsConstructor @NoArgsConstructor
public class AccountDto {

    @NotEmpty
    private String id;
    @NotEmpty
    private String username;
    @NotEmpty
    private String nickname;
    @NotEmpty
    private String password;
    @NotEmpty
    private String email;
    @NotNull
    private Date birthDate;
    @NotNull
    private LocalDateTime createdDate;
}
