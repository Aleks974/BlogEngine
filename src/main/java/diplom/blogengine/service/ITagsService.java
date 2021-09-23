package diplom.blogengine.service;

import diplom.blogengine.api.response.MultipleTagsResponse;

public interface ITagsService {
    MultipleTagsResponse getTagsData(String query);
}
