package me.dblab.twitterclone.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app-properties")
@Getter @Setter
public class AppProperties {

    private String testUsername;
    private String testPassword;
    private String testEmail;
}
