package me.dblab.twitterclone.favorite;

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
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AccountService accountService;
    private final TweetRepository tweetRepository;
    private final AccountRepository accountRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, AccountService accountService, TweetRepository tweetRepository, AccountRepository accountRepository) {
        this.favoriteRepository = favoriteRepository;
        this.accountService = accountService;
        this.tweetRepository = tweetRepository;
        this.accountRepository = accountRepository;
    }

    public Flux<Account> getAccounts(String tweetId) {
        Mono<Tweet> tweetMono = tweetRepository.findById(tweetId);

        return tweetMono.flatMapMany(tw -> favoriteRepository.findAllByTweetId(tweetId))
                .flatMap(fav -> accountRepository.findAllByEmail(fav.getAccountEmail()));

    }

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
                        .flatMap(addCnt -> {
                            addCnt.setCountLike(addCnt.getCountLike() + 1);
                            return tweetRepository.save(addCnt);
                        }).subscribe())
                .map(res -> new ResponseEntity<>(res, HttpStatus.CREATED));

    }

    public Mono<Void> deleteLike(String id) {
        Mono<Favorite> favoriteMono = favoriteRepository.findById(id);

        return favoriteMono
                .map(deleteFavorite ->  tweetRepository.findById(deleteFavorite.getTweetId())
                        .flatMap(delCnt -> {
                            delCnt.setCountLike(delCnt.getCountLike() - 1);
                            return tweetRepository.save(delCnt);
                        }).subscribe()
                ).then(favoriteRepository.deleteById(id));

    }

}
