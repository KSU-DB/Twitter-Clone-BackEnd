package me.dblab.twitterclone.explore;

import lombok.RequiredArgsConstructor;
import me.dblab.twitterclone.tweet.Tweet;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/explores")
public class ExploreController {

    private final ExploreService exploreService;
    private final ExploreValidator exploreValidator;

    @GetMapping
    public Flux<Explore> getSavedExplore()   {
        return exploreService.getSavedExplore();
    }

    @PostMapping("/keywords")
    public Flux<Tweet> getTweetListBySearch(@RequestBody ExploreDto exploreDto)   {
        return Mono.just(exploreDto)
                .filter(this::validate)
                .flatMapMany(exploreService::getTweetListByKeyword)
                .switchIfEmpty(Flux.empty());
    }

    @PostMapping
    public Mono<ResponseEntity> saveKeyword(@RequestBody ExploreDto exploreDto)    {
        return Mono.just(exploreDto)
                .filter(this::validate)
                .flatMap(exploreService::saveKeyword)
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    @DeleteMapping("/{exploreId}")
    public Mono<ResponseEntity<Void>> deleteKeyword(@PathVariable String exploreId) {
        return exploreService.deleteKeyword(exploreId);
    }

    private boolean validate(ExploreDto exploreDto) {
        Errors errors = new BeanPropertyBindingResult(exploreDto, "Explore");
        this.exploreValidator.validate(exploreDto, errors);
        if (errors.hasErrors()) {
            return false;
        }
        return true;
    }

}
