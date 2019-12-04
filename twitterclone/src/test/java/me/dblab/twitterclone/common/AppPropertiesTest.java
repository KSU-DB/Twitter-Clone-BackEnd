package me.dblab.twitterclone.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AppPropertiesTest {

    @Autowired
    AppProperties appProperties;

    @Test
    public void appPropertiesTest() {
        then(appProperties.getTestUsername()).isEqualTo("testUsername");
        then(appProperties.getTestPassword()).isEqualTo("testPassword");
        then(appProperties.getTestEmail()).isEqualTo("test@gmail.com");
    }

}