package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class MultiplePostsResponse {
    private final long count;
    private final List<PostResponse> posts;

    @JsonCreator
    public MultiplePostsResponse(@JsonProperty("count") long count, @JsonProperty("posts") List<PostResponse> posts) {
        this.count = count;
        this.posts = posts;
    }
}
