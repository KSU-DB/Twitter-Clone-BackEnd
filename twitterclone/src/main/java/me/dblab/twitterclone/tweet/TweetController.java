package me.dblab.twitterclone.tweet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tweets")
public class TweetController {

    private final TweetService tweetService;

    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    @GetMapping
    public Flux<Tweet> getTweetList() {
        return tweetService.getTweetList();
    }

    @GetMapping(value = "/{id}")
    public Mono<Tweet> getTweet(@PathVariable String id) {
        return tweetService.getTweet(id);
    }

    @PostMapping
    public Mono<ResponseEntity> saveTweet(@RequestBody Tweet tweet) {
        return tweetService.saveTweet(tweet);
    }

    @PutMapping(value = "/{id}")
    public Mono<ResponseEntity> updateTweet(@PathVariable String id, @RequestBody Tweet tweet) {
        return tweetService.updateTweet(id, tweet);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> deleteTweet(@PathVariable String id) {
        return tweetService.deleteTweet(id);
    }
}
