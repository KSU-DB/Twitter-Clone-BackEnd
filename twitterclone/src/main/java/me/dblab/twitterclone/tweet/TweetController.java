package me.dblab.twitterclone.tweet;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tweets")
public class TweetController {

    private final TweetService tweetService;

    private final TweetValidator tweetValidator;

    @GetMapping
    public Flux<Tweet> getTweetList() {
        return tweetService.getTweetList();
    }

    @GetMapping(value = "/{id}")
    public Mono<Tweet> getTweet(@PathVariable String id) {
        return tweetService.getTweet(id);
    }

    @PostMapping
    public Mono<ResponseEntity> saveTweet(@RequestBody TweetDto tweetDto) {
        if (this.validate(tweetDto)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return tweetService.saveTweet(tweetDto);
    }

    @PutMapping(value = "/{id}")
    public Mono<ResponseEntity<Tweet>> updateTweet(@PathVariable String id, @RequestBody TweetDto tweetDto) {
        if (this.validate(tweetDto)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return tweetService.updateTweet(id, tweetDto);
    }

    @DeleteMapping(value = "/{id}")
    public Mono<ResponseEntity> deleteTweet(@PathVariable String id) {
        return tweetService.deleteTweet(id);
    }

    private boolean validate(TweetDto tweetDto) {
        Errors errors = new BeanPropertyBindingResult(tweetDto, "Tweet");
        this.tweetValidator.validate(tweetDto, errors);
        if (errors.hasErrors()) {
            return true;
        }
        return false;
    }

}
