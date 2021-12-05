package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.context.annotation.PropertySource;

@Getter
public class PostCommentResponse {
    private final long id;
    private final long timestamp;
    private final String text;
    private final UserInfoPhotoResponse user;

    @JsonCreator
    public PostCommentResponse(@JsonProperty("id") long id,
                               @JsonProperty("timestamp") long timestamp,
                               @JsonProperty("text") String text,
                               @JsonProperty("user") UserInfoPhotoResponse user) {
        this.id = id;
        this.timestamp = timestamp;
        this.text = text;
        this.user = user;
    }
}
