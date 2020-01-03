package me.dblab.twitterclone.explore;

import lombok.RequiredArgsConstructor;
import me.dblab.twitterclone.account.AccountRepository;
import me.dblab.twitterclone.account.AccountService;
import me.dblab.twitterclone.tweet.TweetRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExploreService {

    private final ExploreRepository exploreRepository;
    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final TweetRepository tweetRepository;
    private final AccountRepository accountRepository;

    public Flux<Explore> getSavedExplore() {
        return accountService.findCurrentUser()
                .flatMapMany(ac -> exploreRepository.findAllByAccountEmailOrderByKeyword(ac.getEmail())
                        .filter(Explore::isSaved));
    }

    public Flux<Object> getListByKeyword(ExploreDto exploreDto) {
        return saveExplore(exploreDto)
                .flatMap(exploreRepository::save)
                .flatMapMany(exp -> accountRepository.findAllByUsernameContainingOrNicknameContainingOrderByCreatedDate(exp.getKeyword(), exp.getKeyword())
                    .mergeWith(tweetRepository.findAllByContentContainingOrderByCreatedDateDesc(exp.getKeyword())));
    }

    public Mono<ResponseEntity> saveKeyword(ExploreDto exploreDto) {
        return saveExplore(exploreDto)
                .map(explore -> {
                    explore.setSaved(true);
                    return explore;
                })
                .flatMap(exploreRepository::save)
                .map(saveExplore -> new ResponseEntity<>(saveExplore, HttpStatus.CREATED));
    }

    public Mono<ResponseEntity<Void>> deleteKeyword(String id) {
        return accountService.findCurrentUser()
                .flatMap(acc -> exploreRepository.findById(id).filter(exp -> exp.getAccountEmail().equals(acc.getEmail()))
                        .flatMap(acc2 -> exploreRepository.deleteAllByKeywordAndAccountEmail(acc2.getKeyword(), acc2.getAccountEmail())
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST)));
    }

    private Mono<Explore> saveExplore(ExploreDto exploreDto)  {
        return accountService.findCurrentUser()
                .map(ac -> {
                    Explore explore = modelMapper.map(exploreDto, Explore.class);
                    explore.setAccountEmail(ac.getEmail());
                    explore.setSearchedAt(LocalDateTime.now());
                    explore.setSaved(false);
                    return explore;
                });
    }
}
