package me.dblab.twitterclone.common;

import me.dblab.twitterclone.account.AccountDto;
import me.dblab.twitterclone.account.Role;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class BaseControllerTest {

    @Autowired
    public WebTestClient webTestClient;

    @Autowired
    public AppProperties appProperties;

    public AccountDto createAccountDto() {
        return AccountDto.builder()
                .email(appProperties.getTestEmail())
                .username(appProperties.getTestUsername())
                .password(appProperties.getTestPassword())
                .nickname(appProperties.getTestNickname())
                .roles(Collections.singletonList(Role.USER))
                .build();
    }
}
