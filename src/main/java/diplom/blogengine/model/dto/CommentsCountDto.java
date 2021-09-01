package diplom.blogengine.model.dto;

import lombok.Getter;

@Getter
public class CommentsCountDto {
    private final long postId;
    private final long commentCount;

    public CommentsCountDto(long postId, long commentCount) {
        this.postId = postId;
        this.commentCount = commentCount;
    }

}
