package me.dblab.twitterclone.follow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountService;
import me.dblab.twitterclone.tweet.Tweet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;

    private final AccountService accountService;

    public Mono<ResponseEntity<Follow>> following(String email) {
        Mono<Account> currentUser = accountService.findCurrentUser();
        Follow follow = new Follow();
        return accountService.isExistByEmail(email).map(account -> {
            follow.setFollowingEmail(account.getEmail());
            return follow;
        }).flatMap(follow1 ->
            currentUser.map(cu -> {
                follow1.setFollowerEmail(cu.getEmail());
                return follow1;
            })
        ).flatMap(followRepository::save)
                .map(follow1 -> new ResponseEntity<>(follow1, HttpStatus.CREATED))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    public Mono<Void> unfollow(String id) {
        return followRepository.deleteById(id);
    }

    public Flux<Follow> findFollowingEmails(String followerEmail) {
        return followRepository.findAllByFollowerEmail(followerEmail);
    }
}
