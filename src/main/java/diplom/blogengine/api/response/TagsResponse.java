package diplom.blogengine.api.response;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class TagsResponse {
    private final List<TagResponse> tags;

    public TagsResponse(List<TagResponse> tags) {
        this.tags = Collections.unmodifiableList(tags);
    }
}
