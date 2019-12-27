package me.dblab.twitterclone.comment;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/comments", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentController {

    private final CommentService commentService;

    private final CommentValidator commentValidator;

    public CommentController(CommentService commentService, CommentValidator commentValidator) {
        this.commentService = commentService;
        this.commentValidator = commentValidator;
    }

    @GetMapping("/{tweetId}")
    public Flux<Comment> getCommentList(@PathVariable String tweetId) {
        return commentService.getCommentList(tweetId);
    }

    @PostMapping("/{tweetId}")
    public Mono<ResponseEntity<Comment>> saveComment(@PathVariable String tweetId, @RequestBody CommentDto commentDto) {
        return Mono.just(commentDto)
                .filter(this::validate)
                .flatMap(validateComment -> commentService.saveComment(tweetId, validateComment))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    @PutMapping("/{commentId}")
    public Mono<ResponseEntity> updateComment(@PathVariable String commentId, @RequestBody CommentDto commentDto) {
        return Mono.just(commentDto)
                .filter(this::validate)
                .flatMap(validateComment -> commentService.updateComment(commentId, validateComment))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    @DeleteMapping("/{commentId}")
    public Mono<ResponseEntity<Void>> deleteComment(@PathVariable String commentId) {
        return commentService.deleteComment(commentId);
    }

    private boolean validate(CommentDto commentDto) {
        Errors errors = new BeanPropertyBindingResult(commentDto, "Comment");
        commentValidator.validate(commentDto, errors);
        if (errors.hasErrors()) {
            return false;
        }
        return true;
    }
}
