package me.dblab.twitterclone.tweet;

import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class TweetService {

    private final TweetRepository tweetRepository;

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    public TweetService(TweetRepository tweetRepository, AccountService accountService, ModelMapper modelMapper) {
        this.tweetRepository = tweetRepository;
        this.accountService = accountService;
        this.modelMapper = modelMapper;
    }

    public Flux<Tweet> getTweetList() {
        return tweetRepository.findAll();
    }

    public Mono<Tweet> getTweet(String id) {
        return tweetRepository.findById(id);
    }

    public Mono<ResponseEntity> saveTweet(TweetDto tweetDto) {
        return Mono.just(tweetDto).map(tweet1 -> {
            Tweet tweet = modelMapper.map(tweetDto, Tweet.class);
            tweet.setCreatedDate(LocalDateTime.now());
            accountService.findCurrentUser().doOnNext(tweet::setAccount).subscribe();
            return tweet;
        }).flatMap(tweetRepository::save)
                .map(savedTweet -> new ResponseEntity<>(savedTweet, HttpStatus.CREATED));
    }

    public Mono<ResponseEntity> updateTweet(String id, TweetDto tweetDto) {
        return tweetRepository.findById(id)
                .switchIfEmpty(Mono.empty())
                .flatMap(updatedTweet -> {
                    updatedTweet.setContent(tweetDto.getContent());
                    return tweetRepository.save(updatedTweet);
                }).map(updatedTweet -> new ResponseEntity<>(updatedTweet, HttpStatus.OK));
    }

    public Mono<Void> deleteTweet(String id) {
        return tweetRepository.deleteById(id);
    }

}