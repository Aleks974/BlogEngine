package diplom.blogengine.api.response;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class MultiplePostsResponse {
    private final long count;
    private final List<PostResponse> posts;

    public MultiplePostsResponse(long count, List<PostResponse> posts) {
        this.count = count;
        this.posts = Collections.unmodifiableList(posts);
    }
}
