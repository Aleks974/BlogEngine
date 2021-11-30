package diplom.blogengine.service;

import diplom.blogengine.api.response.MultipleTagsResponse;
import diplom.blogengine.model.Tag;

public interface ITagsService {
    MultipleTagsResponse getTagsData(String query);
    Tag getOrSaveNewTag(String t);
}
