package me.dblab.twitterclone.tweet;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {
<<<<<<< HEAD
    Flux<Tweet> findAllByAccount(Account account);
    Mono<Tweet> findByAccount_Email(String email);
=======
    Flux<Tweet> findAllByAuthorEmailOrderByCreatedDateDesc(String email);
    Flux<Tweet> findAllByAuthorEmail(String email);
>>>>>>> a9482d65e7c289f1e9b3a5c11af88ff28f4a22d5
}
