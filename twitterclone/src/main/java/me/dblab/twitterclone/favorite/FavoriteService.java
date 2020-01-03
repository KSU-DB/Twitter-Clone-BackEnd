package me.dblab.twitterclone.favorite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.account.AccountService;
import me.dblab.twitterclone.tweet.Tweet;
import me.dblab.twitterclone.tweet.TweetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AccountService accountService;
    private final TweetRepository tweetRepository;
    private final AccountRepository accountRepository;


    public Flux<Account> getAccounts(String tweetId) {
        Mono<Tweet> tweetMono = tweetRepository.findById(tweetId);

        return tweetMono.flatMapMany(tw -> favoriteRepository.findAllByTweetId(tweetId))
                .flatMap(fav -> accountRepository.findAllByEmail(fav.getAccountEmail()));

    }

    public Mono<ResponseEntity> saveLike(String tweetId) {
        return accountService.findCurrentUser()
                            .flatMap(cu ->
                                favoriteRepository.findByAccountEmailAndTweetId(cu.getEmail(), tweetId)
                                            .map(favorite1 -> ResponseEntity.badRequest().build())
                                            .switchIfEmpty(tweetRepository.findById(tweetId)
                                            .flatMap(tweet -> {
                                                tweet.setCountLike(tweet.getCountLike() + 1);
                                                return tweetRepository.save(tweet);
                                            }).flatMap(tweet -> favoriteRepository.save(
                                                    Favorite.builder()
                                                            .tweetId(tweetId)
                                                            .accountEmail(cu.getEmail())
                                                            .build()
                                                    )).map(favorite -> new ResponseEntity<>(favorite, HttpStatus.CREATED))));
    }

    public Mono<ResponseEntity> deleteLike(String id) {
        Mono<Favorite> favoriteMono = favoriteRepository.findById(id);

        return favoriteMono
                .map(deleteFavorite ->  tweetRepository.findById(deleteFavorite.getTweetId())
                        .flatMap(delCnt -> {
                            delCnt.setCountLike(delCnt.getCountLike() - 1);
                            return tweetRepository.save(delCnt);
                        }).subscribe()
                ).then(favoriteRepository.deleteById(id))
                .map(res -> new ResponseEntity<>(res, HttpStatus.OK));
    }
}
