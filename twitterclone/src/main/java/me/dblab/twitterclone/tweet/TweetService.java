package me.dblab.twitterclone.tweet;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TweetService {

    private final TweetRepository tweetRepository;

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    private final FollowService followService;

    public Flux<Tweet> getTweetList() {
        Mono<Account> currentUser = accountService.findCurrentUser();
        Flux<Tweet> currentUserTweet = currentUser.flatMapMany(cu -> tweetRepository.findAllByAuthorEmail(cu.getEmail()));
        Flux<Tweet> followingUserTweet = currentUser.flatMapMany(cu -> followService.findFollowingEmails(cu.getEmail()))
                .flatMap(follow -> tweetRepository.findAllByAuthorEmailOrderByCreatedDateDesc(follow.getFollowingEmail()));
        return currentUserTweet.mergeWith(followingUserTweet);
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

    public Mono<ResponseEntity<Tweet>> updateTweet(String id, TweetDto tweetDto) {
        return tweetRepository.findById(id)
                .flatMap(updatedTweet -> {
                    updatedTweet.setContent(tweetDto.getContent());
                    return tweetRepository.save(updatedTweet);
                }).map(updatedTweet -> ResponseEntity.ok().body(updatedTweet))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    public Mono<ResponseEntity>  deleteTweet(String id) {
        return accountService.findCurrentUser()
                .flatMap(account -> tweetRepository.findById(id).flatMap(tweet -> {
                        if (tweet.getAuthorEmail().equals(account.getEmail())) {
                            return tweetRepository.deleteById(id).map(i -> ResponseEntity.ok().build());
                        }
                        return Mono.just(ResponseEntity.badRequest().build());
                    }));
    }

}