package me.dblab.twitterclone.favorite;

import lombok.extern.slf4j.Slf4j;
import me.dblab.twitterclone.account.Account;
import me.dblab.twitterclone.account.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AccountService accountService;

    public FavoriteService(FavoriteRepository favoriteRepository, AccountService accountService) {
        this.favoriteRepository = favoriteRepository;
        this.accountService = accountService;
    }

//    public Flux<Favorite> getLike(String tweedId) {
//        return favoriteRepository.findAllByTweetId(tweedId)
//                .map()
//    }

    public Mono<ResponseEntity> saveLike(String tweetId) {
        Mono<Account> currentUser = accountService.findCurrentUser();
        Favorite favorite = new Favorite();
        return currentUser
                .flatMap(cu -> {
                    favorite.setAccountEmail(cu.getEmail());
                    favorite.setTweetId(tweetId);
                    return favoriteRepository.save(favorite);
                })
                .map(res -> new ResponseEntity<>(res, HttpStatus.CREATED));

    }

    public Mono<Void> deleteLike(String id) {
        return favoriteRepository.deleteById(id);
    }
}
