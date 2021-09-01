package diplom.blogengine.service;

import diplom.blogengine.api.response.PostsResponse;
import diplom.blogengine.api.response.mapper.PostsResponseMapper;
import diplom.blogengine.model.dto.PostDto;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.model.dto.CommentsCountDto;
import diplom.blogengine.model.dto.VotesCountDto;
import diplom.blogengine.service.sort.PostSortFactory;
import diplom.blogengine.service.sort.PostSortMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private final PostSortFactory postSortFactory;
    private final PostsResponseMapper postsResponseMapper;

    public PostService(PostRepository postRepository, PostSortFactory postSortFactory, PostsResponseMapper postsResponseMapper) {
        this.postRepository = postRepository;
        this.postSortFactory = postSortFactory;
        this.postsResponseMapper = postsResponseMapper;
    }

    @Override
    public PostsResponse getPostsData(int offset, int limit, PostSortMode mode) {
        Objects.requireNonNull(mode);
        Sort sort = postSortFactory.getSort(mode);
        Pageable page = PageRequest.of(offset, limit, sort);

        Page<PostDto> postsPage;
        if (mode.isPopular()) {
            postsPage = postRepository.findPopularPosts(page);
        } else if (mode.isBest()) {
            postsPage = postRepository.findBestPosts(page);
        } else {
            postsPage = postRepository.findOtherPosts(page);
        }
        List<PostDto> posts = postsPage.getContent();

        PostsResponse postsResponse;
        if (posts == null || posts.isEmpty()) {
            postsResponse = postsResponseMapper.emptyResponse();
        } else {
            List<Long> postsIds = getPostIds(posts);
            postsResponse = postsResponseMapper.postsResponse(postsPage.getTotalElements(),
                                                                posts,
                                                                getPostsCommentsCountMap(postsIds),
                                                                getPostsVotesCountMap(postsIds));
        }
        return postsResponse;

    }

    private List<Long> getPostIds(List<PostDto> posts) {
        return posts.stream().map(p -> p.getId()).collect(Collectors.toList());
    }

    private Map<Long, CommentsCountDto> getPostsCommentsCountMap(List<Long> postsIds) {
        List<CommentsCountDto> commentsCount = postRepository.findCommentsCountByPostIdList(postsIds);
        if (commentsCount == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(commentsCount.stream().collect(Collectors.toMap(c -> c.getPostId(), c -> c)));
    }

    private Map<Long, VotesCountDto> getPostsVotesCountMap(List<Long> postsIds) {
        List<VotesCountDto> votesCount = postRepository.findVotesCountByPostIdList(postsIds);
        if (votesCount == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(votesCount.stream().collect(Collectors.toMap(v -> v.getPostId(), v -> v)));
    }
}
