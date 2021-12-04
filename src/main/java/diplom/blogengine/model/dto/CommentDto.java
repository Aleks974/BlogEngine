package diplom.blogengine.model.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
public class CommentDto {
    private final long id;
    private final LocalDateTime time;
    private final String text;
    private final long userId;
    private final String userName;
    private final String userPhoto;

    public CommentDto(long id, LocalDateTime time, String text, long userId, String userName, String userPhoto) {
        this.id = id;
        this.time = time;
        this.text = text;
        this.userId = userId;
        this.userName = userName;
        this.userPhoto = userPhoto;
    }
}
