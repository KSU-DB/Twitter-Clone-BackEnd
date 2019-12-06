package me.dblab.twitterclone.account;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import java.util.regex.Pattern;

@Component
public class AccountValidator implements Validator {

    @Value("${regex.email}")
    String regexEmail;
    @Value("${regex.password}")
    String regexPassword;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        AccountDto accountDto = (AccountDto) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "required.username", "Username is EMPTY!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nickname", "required.nickname", "Nickname is EMPTY!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "required.password", "Password is EMPTY!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "required.email", "Email is EMPTY!");


        if (accountDto.getUsername().length() < 8 || accountDto.getPassword().length() < 10) {
            errors.rejectValue("username", "Too Short");
        }
        if (!Pattern.matches(regexPassword, accountDto.getPassword().trim())) {
            errors.rejectValue("password", "Not Valid Password");
        }
        if (!Pattern.matches(regexEmail, accountDto.getEmail().trim())) {
            errors.rejectValue("email", "Not Valid Email");
        }
    }
}

