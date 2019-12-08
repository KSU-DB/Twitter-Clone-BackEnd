package me.dblab.twitterclone.follow;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{email}")
    public Mono<ResponseEntity> following(@PathVariable String email) {
        return followService.following(email);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> unfollow(@PathVariable String id) {
        return followService.unfollow(id);
    }
}
