package diplom.blogengine.service;

import diplom.blogengine.api.response.PostsResponse;
import diplom.blogengine.service.sort.PostSortMode;

public interface IPostService {
    PostsResponse getPostsData(int offset, int limit, PostSortMode mode);
}
