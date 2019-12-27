package me.dblab.twitterclone.comment;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CommentValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return CommentDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CommentDto commentDto = (CommentDto) target;

        if (commentDto.getContent() == null) {
            errors.rejectValue("content", "wrongValue", "Content cannot be null.");
            return;
        }

        int contentLength = commentDto.getContent().trim().length();
        if (contentLength < 1 || contentLength > 255) {
            errors.rejectValue("content", "wrongValue", "Content must be at least 1 character long or less than 255 characters long.");
        }
    }
}
