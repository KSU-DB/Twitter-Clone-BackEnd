package me.dblab.twitterclone.follow;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followingEmail}")
    public Mono<ResponseEntity<Follow>> following(@PathVariable String followingEmail) {
        return followService.following(followingEmail);
    }

    @DeleteMapping("/{followId}")
    public Mono<ResponseEntity> unfollow(@PathVariable String followId) {
        return followService.unfollow(followId);
    }
}
