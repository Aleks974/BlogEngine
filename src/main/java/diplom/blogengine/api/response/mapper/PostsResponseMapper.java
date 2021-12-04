package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.*;
import diplom.blogengine.model.Post;
import diplom.blogengine.model.PostComment;
import diplom.blogengine.model.Tag;
import diplom.blogengine.model.User;
import diplom.blogengine.model.dto.CommentDto;
import diplom.blogengine.model.dto.PostDto;
import diplom.blogengine.model.dto.PostDtoExt;
import diplom.blogengine.model.dto.TagDto;
import diplom.blogengine.service.util.TimestampHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class PostsResponseMapper {
    private final Object lock = new Object();
    private final TimestampHelper timestampHelper;
    private volatile MultiplePostsResponse emptyResponse;

    public PostsResponseMapper(TimestampHelper timestampHelper) {
        this.timestampHelper = timestampHelper;
    }

    public MultiplePostsResponse emptyMultiplePostsResponse() {
        log.debug("enter emptyMultiplePostsResponse()");

        //synchronized (lock) { // синхронизация не требуется, т.к. создаваемый объект не изменяемый
            if (emptyResponse == null) {
                emptyResponse = new MultiplePostsResponse(0, List.of());
            }
        //}
        return emptyResponse;
    }

   /* public MultiplePostsResponse multiplePostsResponse(List<PostDto> postsDtoList, long totalPostsCount) {
        log.debug("enter multiplePostsResponse()");

        if (postsDtoList == null || postsDtoList.isEmpty()) {
            return emptyMultiplePostsResponse();
        }
        if (totalPostsCount < 0) {
            throw new IllegalArgumentException("totalElements < 0");
        }
        List<PostResponse> postsResponseList = postsDtoList.stream().map(this::postDtoToResponse)
                                                    .collect(Collectors.toList());
        return new MultiplePostsResponse(totalPostsCount, postsResponseList);
    }
*/
    public MultiplePostsResponse multiplePostsResponse(Stream<PostResponse> postResponses, long totalPostsCount) {
        log.debug("enter multiplePostsResponse()");

        return new MultiplePostsResponse(totalPostsCount, postResponses.collect(Collectors.toList()));
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

    public SinglePostResponse singlePostResponse(PostDtoExt postDto, List<CommentDto> comments, Set<TagDto> tags) {
        log.debug("enter singlePostResponse()");

        Objects.requireNonNull(postDto, "postDto is null ");
        UserInfoResponse userResponse = new UserInfoResponse(postDto.getUserId(), postDto.getUserName());
        List<PostCommentResponse> commentsResponses;
        if (comments != null) {
            commentsResponses = comments.stream().map(this::commentDtoToResponse).collect(Collectors.toList());
        } else {
            commentsResponses = Collections.emptyList();
        }
        Set<String> tagsResponses;
        if (tags != null) {
            tagsResponses = tags.stream().map(TagDto::getName).collect(Collectors.toSet());
        } else {
            tagsResponses = Collections.emptySet();
        }
        return SinglePostResponse.builder()
                .id(postDto.getId())
                .timestamp(timestampHelper.toTimestampAtServerZone(postDto.getTime()))
                .active(postDto.isActive())
                .user(userResponse)
                .title(postDto.getTitle())
                .text(postDto.getText())
                .likeCount(postDto.getLikeCount())
                .dislikeCount(postDto.getDislikeCount())
                .viewCount(postDto.getViewCount())
                .comments(commentsResponses)
                .tags(tagsResponses)
                .build();
    }

    private PostCommentResponse commentDtoToResponse(CommentDto commentDto) {
        log.debug("enter commentToResponse()");
        Objects.requireNonNull(commentDto);
        UserInfoPhotoResponse userResponse = new UserInfoPhotoResponse(commentDto.getUserId(),
                                                            commentDto.getUserName(),
                                                            commentDto.getUserPhoto());
        return new PostCommentResponse(commentDto.getId(),
                                        timestampHelper.toTimestampAtServerZone(commentDto.getTime()),
                                        commentDto.getText(),
                                        userResponse);
    }

}
