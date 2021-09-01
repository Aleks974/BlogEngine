package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.PostResponse;
import diplom.blogengine.api.response.UserInfoResponse;
import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.model.dto.PostDto;
import diplom.blogengine.model.dto.UserDto;
import diplom.blogengine.service.util.IContentProcessor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PostResponseMapper {
    private final BlogSettings blogSettings;
    private final IContentProcessor contentProcessor;

    public PostResponseMapper(BlogSettings blogSettings, IContentProcessor contentProcessor) {
        this.blogSettings = blogSettings;
        this.contentProcessor = contentProcessor;
    }

    public PostResponse postResponse(PostDto post, long commentCount, long likeCount, long dislikeCount) {
        UserDto user = Objects.requireNonNull(post.getUser(), "User is null for post " + post.getId());
        UserInfoResponse userInfoResponse = new UserInfoResponse(user.getId(), Objects.requireNonNull(user.getName()));
        String title = Objects.requireNonNull(post.getTitle(), "Title is null for post " + post.getId());

        return PostResponse.builder()
                .id(post.getId())
                .timestamp(post.getTimestamp())
                .user(userInfoResponse)
                .title(title)
                .announce(post.getAnnounce())
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .commentCount(commentCount)
                .viewCount(post.getViewCount())
                .build();
    }



}
