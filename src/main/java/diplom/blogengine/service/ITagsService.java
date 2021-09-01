package diplom.blogengine.service;

import diplom.blogengine.api.response.TagsResponse;

public interface ITagsService {
    TagsResponse getTagsData(String query);
}
