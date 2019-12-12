package me.dblab.twitterclone.favorite;

import lombok.RequiredArgsConstructor;
import me.dblab.twitterclone.account.Account;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tweet/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/{tweetId}")
    public Flux<Account> getLikeAccounts(@PathVariable String tweetId)   {
        return favoriteService.getAccounts(tweetId);
    }

    @PostMapping("/{tweetId}")
    public Mono<ResponseEntity> likeTweet(@PathVariable String tweetId)   {
        return favoriteService.saveLike(tweetId);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> unlikeTweet(@PathVariable String id)   {
        return favoriteService.deleteLike(id);
    }




}
