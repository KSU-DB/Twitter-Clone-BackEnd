package me.dblab.twitterclone.tweet;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TweetValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return TweetDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TweetDto tweetDto = (TweetDto) target;
        int tweetContentLength = tweetDto.getContent().trim().length();
        if (tweetContentLength < 1 || tweetContentLength > 255) {
            errors.rejectValue("content", "Content must be at least 1 character long or less than 255 characters long.");
        }
    }
}
