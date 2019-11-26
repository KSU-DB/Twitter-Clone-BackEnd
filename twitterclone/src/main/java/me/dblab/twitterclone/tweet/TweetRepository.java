package me.dblab.twitterclone.tweet;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {
}
