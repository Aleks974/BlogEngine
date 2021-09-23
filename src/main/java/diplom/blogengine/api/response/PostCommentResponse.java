package diplom.blogengine.api.response;

import lombok.Getter;

@Getter
public class PostCommentResponse {
    private final long id;
    private final long timestamp;
    private final String text;
    private final UserInfoPhotoResponse user;

    public PostCommentResponse(long id, long timestamp, String text, UserInfoPhotoResponse user) {
        this.id = id;
        this.timestamp = timestamp;
        this.text = text;
        this.user = user;
    }
}
