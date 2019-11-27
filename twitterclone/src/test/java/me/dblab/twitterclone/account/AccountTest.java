package me.dblab.twitterclone.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class AccountTest {

    @Test
    public void accountBuilder()    {

        // Given
        String username = "username test";
        String nickname = "nickname test";
        String password = "password test";
        String email = "email test";
        LocalDateTime createdDate = LocalDateTime.of(2019, 11, 25, 21, 54);

        // When
        Account account = Account.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .nickname(nickname)
                .password(password)
                .email(email)
                .birthDate(Date.from(Instant.now()))
                .createdDate(createdDate)
                .build();

        // Then
        assertThat(account).isNotNull();
        assertThat(account.getUsername()).isEqualTo(username);
        assertThat(account.getNickname()).isEqualTo(nickname);
        assertThat(account.getPassword()).isEqualTo(password);
        assertThat(account.getEmail()).isEqualTo(email);
        assertThat(account.getCreatedDate()).isEqualTo(createdDate);
    }

}
