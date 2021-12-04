package diplom.blogengine.model.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostDtoExt extends PostDto {
    private final String text;
    private final boolean isActive;
    public PostDtoExt(long id, LocalDateTime time, String title, String announce, long likeCount, long dislikeCount, long commentCount, int viewCount, String text, boolean isActive, long userId, String name) {
        super(id, time, title, announce, likeCount, dislikeCount, commentCount, viewCount, userId, name);
        this.isActive = isActive;
        this.text = text;
    }
}
