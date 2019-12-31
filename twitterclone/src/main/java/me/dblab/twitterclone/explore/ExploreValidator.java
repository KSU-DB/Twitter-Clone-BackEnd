package me.dblab.twitterclone.explore;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ExploreValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return ExploreDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ExploreDto exploreDto = (ExploreDto) target;

        if (exploreDto.getKeyword() == null || "".equals(exploreDto.getKeyword().trim())) {
            errors.rejectValue("keyword", "wrongValue", "Keyword cannot be null.");
        }
    }
}
