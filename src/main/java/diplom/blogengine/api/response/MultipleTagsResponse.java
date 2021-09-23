package diplom.blogengine.api.response;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class MultipleTagsResponse {
    private final List<TagResponse> tags;

    public MultipleTagsResponse(List<TagResponse> tags) {
        this.tags = Collections.unmodifiableList(tags);
    }
}
