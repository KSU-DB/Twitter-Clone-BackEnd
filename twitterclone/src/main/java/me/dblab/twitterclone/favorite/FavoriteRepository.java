package me.dblab.twitterclone.favorite;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FavoriteRepository extends ReactiveMongoRepository<Favorite, String> {

    Flux<Favorite> findAllByTweetId(String tweetId);
    Mono<Favorite> findByTweetId(String tweetId);   // test용

}
