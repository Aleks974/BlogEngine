package diplom.blogengine.api.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostResponse {
    private long id;
    private long timestamp;
    private UserInfoResponse user;
    private String title;
    private String announce;
    private long likeCount;
    private long dislikeCount;
    private long commentCount;
    private long viewCount;
}
