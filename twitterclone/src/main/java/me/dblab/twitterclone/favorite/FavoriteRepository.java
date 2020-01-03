package me.dblab.twitterclone.favorite;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FavoriteRepository extends ReactiveMongoRepository<Favorite, String> {

    Flux<Favorite> findAllByTweetId(String tweetId);
    Mono<Favorite> findByAccountEmail(String email); // test용
    Mono<Favorite> findByAccountEmailAndTweetId(String accountEmail, String tweetId);
}
