package me.dblab.twitterclone.tweet;

import me.dblab.twitterclone.account.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {
    Flux<Tweet> findAllByAccount(Account account);
    Mono<Tweet> findByAccount_Email(String email);
}
