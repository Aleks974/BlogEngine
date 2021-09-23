package diplom.blogengine.service;

import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.api.response.CalendarPostsResponse;
import diplom.blogengine.api.response.MultiplePostsResponse;
import diplom.blogengine.service.sort.PostSortMode;

public interface IPostService {
    MultiplePostsResponse getPostsData(int offset, int limit, PostSortMode mode);

    MultiplePostsResponse getPostsDataByQuery(int offset, int limit, String query);

    MultiplePostsResponse getPostsDataByDate(int offset, int limit, String date);

    MultiplePostsResponse getPostsDataByTag(int offset, int limit, String tag);

    CalendarPostsResponse getCalendarDataByYear(Integer year);

    SinglePostResponse getPostDataById(long id);

    void test();
}
