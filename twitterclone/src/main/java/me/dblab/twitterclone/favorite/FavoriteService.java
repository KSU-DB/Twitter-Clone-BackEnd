package me.dblab.twitterclone.favorite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountService;
import me.dblab.twitterclone.tweet.Tweet;
import me.dblab.twitterclone.tweet.TweetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AccountService accountService;
    private final TweetRepository tweetRepository;

    public Mono<ResponseEntity> saveLike(String tweetId) {
        Mono<Account> currentUser = accountService.findCurrentUser();
        Favorite favorite = new Favorite();

        return currentUser
                .flatMap(cu -> {
                    favorite.setAccountEmail(cu.getEmail());
                    favorite.setTweetId(tweetId);
                    return favoriteRepository.save(favorite);
                })
                .doOnNext(updateCnt -> tweetRepository.findById(tweetId)
                        .flatMap(cnt -> {
                            cnt.setCountLike(cnt.getCountLike() + 1);
                            return tweetRepository.save(cnt);
                        }).subscribe())
                .map(res -> new ResponseEntity<>(res, HttpStatus.CREATED));

    }

    public Mono<Void> deleteLike(String id) {
        return favoriteRepository.deleteById(id);
    }

}
