package me.dblab.twitterclone.comment;

import lombok.*;

@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class CommentDto {

    private String content;
}
