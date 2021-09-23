package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.TagResponse;
import diplom.blogengine.api.response.MultipleTagsResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class TagsResponseMapper {
    private final Object lock = new Object();
    private volatile MultipleTagsResponse emptyResponse;

    public MultipleTagsResponse emptyResponse() {
        MultipleTagsResponse response = emptyResponse;
        if (response == null) {
            synchronized (lock) {
                if (emptyResponse == null) {
                    emptyResponse = response = new MultipleTagsResponse(Collections.emptyList());
                }
            }
        }
        return response;
    }

    public MultipleTagsResponse tagsResponse(List<TagResponse> tags) {
        if (tags == null || tags.isEmpty()) {
            return emptyResponse;
        }
        return new MultipleTagsResponse(tags);
    }
}
