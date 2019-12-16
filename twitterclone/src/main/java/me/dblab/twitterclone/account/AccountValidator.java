package me.dblab.twitterclone.account;

import me.dblab.twitterclone.common.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.util.regex.Pattern;

@Component
public class AccountValidator implements Validator {

    private final AppProperties appProperties;

    public AccountValidator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AccountDto accountDto = (AccountDto) target;

        if(accountDto.getUsername() == null)   {
            errors.rejectValue("username", "username must not be null");
            return;
        }
        if(accountDto.getNickname() == null)   {
            errors.rejectValue("nickname", "nickname must not be null");
            return;
        }
        if(accountDto.getPassword() == null)   {
            errors.rejectValue("password", "password must not be null");
            return;
        }
        if(accountDto.getEmail() == null)   {
            errors.rejectValue("email", "email must not be null");
            return;
        }
        if (accountDto.getUsername().length() < 8) {
            errors.rejectValue("username", "Too Short");
        }
        if(accountDto.getPassword().length() < 10)  {
            errors.rejectValue("password", "Too Short");
        }
        if (!Pattern.matches(appProperties.getRegexPassword(), accountDto.getPassword().trim())) {
            errors.rejectValue("password", "Not Valid Password");
        }
        if (!Pattern.matches(appProperties.getRegexEmail(), accountDto.getEmail().trim())) {
            errors.rejectValue("email", "Not Valid Email");
        }
    }
}

