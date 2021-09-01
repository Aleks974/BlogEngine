package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.PostResponse;
import diplom.blogengine.api.response.PostsResponse;
import diplom.blogengine.api.response.mapper.PostResponseMapper;
import diplom.blogengine.model.dto.PostDto;
import diplom.blogengine.model.dto.CommentsCountDto;
import diplom.blogengine.model.dto.VotesCountDto;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PostsResponseMapper {
    private final Object lock = new Object();
    private final PostResponseMapper postResponseMapper;
    private volatile PostsResponse emptyResponse;

    public PostsResponseMapper(PostResponseMapper postResponseMapper) {
        this.postResponseMapper = postResponseMapper;
    }

    public PostsResponse emptyResponse() {
        PostsResponse response = emptyResponse;
        if (response == null) {
            synchronized (lock) {
                if (emptyResponse == null) {
                    emptyResponse = response = new PostsResponse(0, Collections.emptyList());
                }
            }
        }
        return response;
    }


    public PostsResponse postsResponse(long totalElements,
                                       List<PostDto> posts,
                                       Map<Long, CommentsCountDto> postsCommentsCountMap,
                                       Map<Long, VotesCountDto> postsVotesCountMap) {
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements < 0");
        }
        Objects.requireNonNull(posts);
        Objects.requireNonNull(postsCommentsCountMap);
        Objects.requireNonNull(postsVotesCountMap);

        List<PostResponse> postsResponseList = posts.stream()
             .map(p -> PostToResponseConversion(p, postsCommentsCountMap, postsVotesCountMap))
             .collect(Collectors.toList());
        return new PostsResponse(totalElements, postsResponseList);
    }


    private PostResponse PostToResponseConversion(PostDto post,
                                                  Map<Long, CommentsCountDto> postsCommentsCountMap,
                                                  Map<Long, VotesCountDto> postsVotesCountMap) {
        Objects.requireNonNull(post);
        long id = post.getId();
        long commentCount = 0;
        CommentsCountDto ccDto;
        if ((ccDto = postsCommentsCountMap.get(id)) != null) {
            commentCount = ccDto.getCommentCount();
        }
        long likeCount = 0;
        long dislikeCount = 0;
        VotesCountDto vcDto;
        if ((vcDto = postsVotesCountMap.get(id)) != null) {
            likeCount = vcDto.getLikeCount();
            dislikeCount = vcDto.getDislikeCount();
        }

        return postResponseMapper.postResponse(post, commentCount, likeCount, dislikeCount);
    }

}
