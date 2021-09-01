package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.TagResponse;
import diplom.blogengine.api.response.TagsResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class TagsResponseMapper {
    private final Object lock = new Object();
    private volatile TagsResponse emptyResponse;

    public TagsResponse emptyResponse() {
        TagsResponse response = emptyResponse;
        if (response == null) {
            synchronized (lock) {
                if (emptyResponse == null) {
                    emptyResponse = response = new TagsResponse(Collections.emptyList());
                }
            }
        }
        return response;
    }

    public TagsResponse tagsResponse(List<TagResponse> tags) {
        if (tags == null || tags.isEmpty()) {
            return emptyResponse;
        }
        return new TagsResponse(tags);
    }
}
