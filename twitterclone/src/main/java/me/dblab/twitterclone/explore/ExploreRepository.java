package me.dblab.twitterclone.explore;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExploreRepository extends ReactiveMongoRepository<Explore, String> {
    Flux<Explore> findAllByAccountEmailOrderByKeyword(String email);
    Mono<Void> deleteAllByKeywordAndAccountEmail(String keyword, String accountEmail);
}
