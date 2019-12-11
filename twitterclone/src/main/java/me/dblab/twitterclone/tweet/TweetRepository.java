package me.dblab.twitterclone.tweet;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {
    Flux<Tweet> findAllByAuthorEmailOrderByCreatedDateDesc(String email);
    Flux<Tweet> findAllByAuthorEmail(String email);
}
