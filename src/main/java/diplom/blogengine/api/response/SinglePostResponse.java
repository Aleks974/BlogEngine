package diplom.blogengine.api.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import diplom.blogengine.model.Tag;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@Builder
@JsonDeserialize(builder = SinglePostResponse.SinglePostResponseBuilder.class)
public class SinglePostResponse {
    private long id;
    private long timestamp;
    private boolean active;
    private UserInfoResponse user;
    private String title;
    private String text;
    private long likeCount;
    private long dislikeCount;
    private long viewCount;
    private List<PostCommentResponse> comments;
    private Set<String> tags;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SinglePostResponseBuilder {

    }

}
