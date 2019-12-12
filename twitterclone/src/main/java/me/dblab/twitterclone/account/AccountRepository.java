package me.dblab.twitterclone.account;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {
    Mono<Account> findByEmail(String email);
    Flux<Account> findAllByEmail(String email);
}
