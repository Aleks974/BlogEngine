package diplom.blogengine.model.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
public class PostDto {
    private final long id;
    private final LocalDateTime time;
    private final String title;
    private final String announce;
    private final long dislikeCount;
    private final long likeCount;
    private final long commentCount;
    private int viewCount;
    private final long userId;
    private final String userName;

    public PostDto(long id, LocalDateTime time, String title, String announce, long likeCount, long dislikeCount, long commentCount, int viewCount, long userId, String userName) {
        this.id = id;
        this.time = time;
        this.title = title;
        this.announce = announce;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.userId = userId;
        this.userName = userName;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
