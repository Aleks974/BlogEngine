package diplom.blogengine.model.dto;

import lombok.Getter;

@Getter
public class VotesCountDto {
    private final long postId;
    private final long likeCount;
    private final long dislikeCount;

    public VotesCountDto(long postId, long likeCount, long dislikeCount) {
        this.postId = postId;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }
}
