package me.dblab.twitterclone.follow;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FollowRepository extends ReactiveMongoRepository<Follow, String> {
    Flux<Follow> findAllByFollowerEmail(String followerEmail);
    Mono<Void> deleteByFollowerEmailAndFollowingEmail(String followerEmail, String followingEmail);
    Mono<Follow> findByFollowingEmail(String followingEmail); //테스트를 위해 생성, 비즈니스 로직에서는 사용하지 말 것!
}
