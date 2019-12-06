package me.dblab.twitterclone.tweet;

import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.account.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class TweetService {

    private final TweetRepository tweetRepository;

    private final AccountService accountService;

    public TweetService(TweetRepository tweetRepository, AccountService accountService) {
        this.tweetRepository = tweetRepository;
        this.accountService = accountService;
    }

    public Flux<Tweet> getTweetList() {
        return tweetRepository.findAll();
    }

    public Mono<Tweet> getAnTweet(String id) {
        return tweetRepository.findById(id);
    }

    public Mono<ResponseEntity> saveTweet(Tweet tweet) {
        return Mono.just(tweet).map(tweet1 -> {
            tweet1.setCreatedDate(LocalDateTime.now());
            Mono<Account> byEmail = accountService.findCurrentUser();
            byEmail.doOnNext(tweet1::setAccount).subscribe();
            return tweet1;
        }).flatMap(tweetRepository::save)
                .map(savedTweet -> new ResponseEntity<>(savedTweet, HttpStatus.CREATED));
    }

    public Mono<ResponseEntity> updateTweet(String id, Tweet tweet) {
        return tweetRepository.findById(id)
                .switchIfEmpty(Mono.empty())
                .flatMap(updatedTweet -> {
                    updatedTweet.setContent(tweet.getContent());
                    return tweetRepository.save(updatedTweet);
                }).map(updatedTweet -> new ResponseEntity<>(updatedTweet, HttpStatus.OK));
    }

    public Mono<Void> deleteTweet(String id) {
        return tweetRepository.deleteById(id);
    }

}