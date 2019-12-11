package me.dblab.twitterclone.tweet;

import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountService;
import me.dblab.twitterclone.follow.FollowService;
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

    private final FollowService followService;

    public TweetService(TweetRepository tweetRepository, AccountService accountService, ModelMapper modelMapper, FollowService followService) {
        this.tweetRepository = tweetRepository;
        this.accountService = accountService;
        this.modelMapper = modelMapper;
        this.followService = followService;
    }

    public Flux<Tweet> getTweetList() {
        Mono<Account> currentUser = accountService.findCurrentUser();
        return currentUser.flatMapMany(cu -> followService.findFollowingEmails(cu.getEmail()))
                .flatMap(follow -> tweetRepository.findAllByAuthorEmailOrderByCreatedDateDesc(follow.getFollowingEmail()));
    }

    public Mono<Tweet> getTweet(String id) {
        return tweetRepository.findById(id);
    }

    public Mono<ResponseEntity> saveTweet(TweetDto tweetDto) {
        return accountService.findCurrentUser()
                .map(cu -> {
                    Tweet tweet = modelMapper.map(tweetDto, Tweet.class);
                    tweet.setCreatedDate(LocalDateTime.now());
                    tweet.setAuthorEmail(cu.getEmail());
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