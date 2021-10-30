package diplom.blogengine.service;

import diplom.blogengine.api.request.PostCommentDataRequest;
import diplom.blogengine.api.request.PostDataRequest;
import diplom.blogengine.api.request.PostModerationRequest;
import diplom.blogengine.api.request.VoteDataRequest;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.api.response.CalendarPostsResponse;
import diplom.blogengine.api.response.MultiplePostsResponse;
import diplom.blogengine.model.ModerationStatus;
import diplom.blogengine.security.UserDetailsExt;

import java.util.Locale;

public interface IPostService {
    MultiplePostsResponse getPostsData(int offset, int limit, PostSortMode mode);

    MultiplePostsResponse getMyPostsData(int offset, int limit, MyPostStatus myPostStatus, UserDetailsExt authUser);

    MultiplePostsResponse getPostsDataByQuery(int offset, int limit, String query);

    MultiplePostsResponse getPostsDataByDate(int offset, int limit, String date);

    MultiplePostsResponse getPostsDataByTag(int offset, int limit, String tag);

    CalendarPostsResponse getCalendarDataByYear(Integer year);

    SinglePostResponse getPostDataById(long postId, UserDetailsExt authUser);

    MultiplePostsResponse getModerationPostsData(int offset, int limit, ModerationStatus status, UserDetailsExt authUser);

    ResultResponse newPost(PostDataRequest postDataRequest, UserDetailsExt authUser, boolean moderationIsEnabled);

    ResultResponse updatePost(long id, PostDataRequest postDataRequest, UserDetailsExt authUser, boolean moderationIsEnabled);

    ResultResponse newComment(PostCommentDataRequest commentDataRequest, UserDetailsExt authUser, Locale locale);

    ResultResponse newVote(VoteParameter voteParam, VoteDataRequest voteDataRequest, UserDetailsExt authenticatedUser, Locale locale);

    ResultResponse moderatePost(PostModerationRequest postModerationRequest, UserDetailsExt authenticatedUser);
}
