package me.dblab.twitterclone.comment;

import me.dblab.twitterclone.account.AccountService;
import me.dblab.twitterclone.tweet.TweetService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    public CommentService(CommentRepository commentRepository, AccountService accountService, ModelMapper modelMapper) {
        this.commentRepository = commentRepository;
        this.accountService = accountService;
        this.modelMapper = modelMapper;
    }

    public Mono<ResponseEntity<Comment>> saveComment(String tweetId, CommentDto commentDto) {
        return accountService.findCurrentUser()
                .map(cu -> {
                    Comment comment = modelMapper.map(commentDto, Comment.class);
                    comment.setCreatedAt(LocalDateTime.now());
                    comment.setAuthorEmail(cu.getEmail());
                    comment.setTweetId(tweetId);
                    return comment;
                }).flatMap(commentRepository::save)
                .map(savedComment -> new ResponseEntity<>(savedComment, HttpStatus.CREATED))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    public Mono<ResponseEntity> updateComment(String commentId, CommentDto validateComment) {
        return commentRepository.findById(commentId)
                .map(comment -> {
                    comment.setContent(validateComment.getContent());
                    return comment;
                }).flatMap(commentRepository::save)
                .map(comment -> new ResponseEntity<>(comment, HttpStatus.OK));
    }

    public Mono<ResponseEntity<Void>> deleteComment(String commentId) {
        return commentRepository.findById(commentId)
                .flatMap(comment -> commentRepository.delete(comment).then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST)));
    }

    Flux<Comment> getCommentList(String tweetId) {
        return commentRepository.findAllByTweetId(tweetId);
    }
}
