package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.*;
import diplom.blogengine.model.Post;
import diplom.blogengine.model.PostComment;
import diplom.blogengine.model.Tag;
import diplom.blogengine.model.User;
import diplom.blogengine.service.util.ContentHelper;
import diplom.blogengine.service.util.TimestampHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostsResponseMapper {
    private final Object lock = new Object();
    private final TimestampHelper timestampHelper;
    private final ContentHelper contentHelper;
    private volatile MultiplePostsResponse emptyResponse;

    public PostsResponseMapper(TimestampHelper timestampHelper, ContentHelper contentHelper) {
        this.timestampHelper = timestampHelper;
        this.contentHelper = contentHelper;
    }

    public MultiplePostsResponse emptyMultiplePostsResponse() {
        log.debug("enter emptyMultiplePostsResponse()");

        MultiplePostsResponse response = emptyResponse;
        if (response == null) {
            synchronized (lock) {
                if (emptyResponse == null) {
                    emptyResponse = response = new MultiplePostsResponse(0, Collections.emptyList());
                }
            }
        }
        return response;
    }

    public MultiplePostsResponse multiplePostsResponse(List<Object[]> postsDataList, long totalPostsCount) {
        log.debug("enter multiplePostsResponse()");

        if (postsDataList == null || postsDataList.isEmpty()) {
            return emptyMultiplePostsResponse();
        }
        if (totalPostsCount < 0) {
            throw new IllegalArgumentException("totalElements < 0");
        }
        List<PostResponse> postsResponseList = postsDataList.stream().map(this::postDataToResponse)
                                                    .collect(Collectors.toList());
        return new MultiplePostsResponse(totalPostsCount, postsResponseList);
    }

    private PostResponse postDataToResponse(Object[] postData) {
        log.debug("enter postDataToResponse()");

        Objects.requireNonNull(postData, "input postData is null");
        final int POSTDATA_SIZE = 5;
        if ( postData.length != POSTDATA_SIZE) {
            throw new IllegalArgumentException("size of postData is not 5 ");
        }

        final int POST_INDEX = 0;
        final int LIKE_COUNT_INDEX = 2;
        final int DISLIKE_COUNT_INDEX = 3;
        final int COMMENT_COUNT_INDEX = 4;
        Post post = (Post)(postData[POST_INDEX]);
        Objects.requireNonNull(post, "postDataToResponse(): post is null");
        long likeCount = (Long)(postData[LIKE_COUNT_INDEX]);
        long dislikeCount = (Long)(postData[DISLIKE_COUNT_INDEX]);
        long commentCount = (Long)(postData[COMMENT_COUNT_INDEX]);

        log.debug("postDataToResponse(): post: {}, likeCount: {}, dislikeCount: {}, commentCount: {}", post, likeCount, dislikeCount, commentCount);

        long id = post.getId();
        User user = Objects.requireNonNull(post.getUser(), "User is null for post " + id);
        UserInfoResponse userInfoResponse = new UserInfoResponse(user.getId(), Objects.requireNonNull(user.getName()));
        String title = Objects.requireNonNull(post.getTitle(), "Title is null for post " + id);

        return PostResponse.builder()
                .id(id)
                .timestamp(timestampHelper.toTimestampAtServerZone(post.getTime()))
                .user(userInfoResponse)
                .title(title)
                .announce(post.getAnnounce(contentHelper))
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .commentCount(commentCount)
                .viewCount(post.getViewCount())
                .build();
    }

    public CalendarPostsResponse calendarPostsResponse(List<Integer> calendarYears, List<Object[]> calendarPostsData) {
        log.debug("enter calendarPostsResponse()");

        final int CALENDAR_POST_DATA_SIZE = 2;
        final String OUTPUT_DATE_FORMAT = "yyyy-MM-dd";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(OUTPUT_DATE_FORMAT);
        dateFormatter.setLenient(false);

        Map<String, Long> postsCountPerDates = new TreeMap<>(Comparator.reverseOrder());
        if (calendarPostsData != null ) {
            for (Object[] calendarPostData : calendarPostsData) {
                if (calendarPostData.length != CALENDAR_POST_DATA_SIZE) {
                    throw new IllegalArgumentException("calendarPostsResponse(): size of calendarPostData is not 2");
                }
                Date date = (Date) calendarPostData[0];
                Long count = (long) calendarPostData[1];
                postsCountPerDates.put(dateFormatter.format(date), count);
            }
        }

        return new CalendarPostsResponse(calendarYears, postsCountPerDates);
    }

    public SinglePostResponse singlePostResponse(Post post, long likeCount, long dislikeCount) {
        log.debug("enter singlePostResponse()");

        User user = Objects.requireNonNull(post.getUser(), "Post user is null " + post.getId());
        UserInfoResponse userResponse = new UserInfoResponse(user.getId(), user.getName());
        List<PostCommentResponse> comments = Collections.emptyList();
        if (post.getComments() != null) {
           comments = post.getComments().stream().map(this::commentToResponse).collect(Collectors.toList());
        }
        Set<String> tags = Collections.emptySet();
        if (post.getTags() != null) {
            tags = post.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
        }
        return SinglePostResponse.builder()
                .id(post.getId())
                .timestamp(timestampHelper.toTimestampAtServerZone(post.getTime()))
                .active(post.isActive())
                .user(userResponse)
                .title(post.getTitle())
                .text(post.getText())
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .viewCount(post.getViewCount())
                .comments(comments)
                .tags(tags)
                .build();
    }

    private PostCommentResponse commentToResponse(PostComment comment) {
        log.debug("enter commentToResponse()");

        User user = Objects.requireNonNull(comment.getUser(), "Comment user is null " + comment.getId());
        UserInfoPhotoResponse userResponse = new UserInfoPhotoResponse(user.getId(), user.getName(), user.getPhoto());
        return new PostCommentResponse(comment.getId(),
                                        timestampHelper.toTimestampAtServerZone(comment.getTime()),
                                        comment.getText(),
                                        userResponse);
    }

}
