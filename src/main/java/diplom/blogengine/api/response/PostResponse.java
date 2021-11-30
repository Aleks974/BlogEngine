package diplom.blogengine.api.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = PostResponse.PostResponseBuilder.class)
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

    @JsonPOJOBuilder(withPrefix = "")
    public static class PostResponseBuilder {

    }
}
