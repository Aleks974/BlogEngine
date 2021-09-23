package diplom.blogengine.service;

import diplom.blogengine.api.response.CalendarPostsResponse;
import diplom.blogengine.api.response.MultiplePostsResponse;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.api.response.mapper.PostsResponseMapper;
import diplom.blogengine.exception.PostNotFoundException;
import diplom.blogengine.exception.RequestParamDateParseException;
import diplom.blogengine.model.Post;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.service.sort.PostSortMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private final PostsResponseMapper postsResponseMapper;
    private final int CURRENT_YEAR = LocalDate.now().getYear();

    public PostService(PostRepository postRepository, PostsResponseMapper postsResponseMapper) {
        this.postRepository = postRepository;
        this.postsResponseMapper = postsResponseMapper;
    }

    @Override
    public MultiplePostsResponse getPostsData(int offset, int limit, PostSortMode mode) {
        log.debug("enter getPostsData()");

        Pageable pageRequest;
        List<Object[]> postsDataList;
        long totalPostsCount;
        pageRequest = getPageRequest(offset, limit, mode);
        if (mode == PostSortMode.BEST) {
            postsDataList = postRepository.findPostsDataOrderByLikesCount(pageRequest);
            totalPostsCount = postRepository.getTotalPostsCountExcludeDislikes();
        } else if (mode == PostSortMode.POPULAR) {
            postsDataList = postRepository.findPostsDataOrderByCommentsCount(pageRequest);
            totalPostsCount = postRepository.getTotalPostsCount();
        } else  {
            postsDataList = postRepository.findPostsData(pageRequest);
            totalPostsCount = postRepository.getTotalPostsCount();
        }

        return postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
    }


    private Pageable getPageRequest(int offset, int limit, PostSortMode mode) {
        log.debug("enter getPageRequest()");

        Pageable pageRequest;
        final String TIME_FIELD = "time";
        //final String LIKE_COUNT_FIELD = "like_count";
        //final String COMMENT_COUNT_FIELD = "comment_count";

        if (mode == PostSortMode.BEST || mode == PostSortMode.POPULAR) {
            pageRequest = PageRequest.of(offset, limit);
        } else {
            Sort sort;
            if (mode == PostSortMode.EARLY) {
                sort = Sort.by(Sort.Direction.ASC, TIME_FIELD);
            } else if (mode == PostSortMode.RECENT) {
                sort = Sort.by(Sort.Direction.DESC, TIME_FIELD);
            } else {
                sort = Sort.by(Sort.Direction.ASC, TIME_FIELD);
            }
            pageRequest = PageRequest.of(offset, limit, sort);
        }

        return pageRequest;
    }


    public MultiplePostsResponse getPostsDataByQuery(int offset, int limit, String query) {
        log.debug("enter getPostsDataByQuery()");

        MultiplePostsResponse response = null;
        if (query == null || query.isEmpty()) {
            response = getPostsData(offset, limit, PostSortMode.RECENT);
        } else {
            Pageable pageRequestWithSort = getPageRequest(offset, limit, PostSortMode.RECENT);
            List<Object[]> postsDataList = postRepository.findPostsByQuery(query, pageRequestWithSort);
            long totalPostsCount = postRepository.getTotalPostsCountByQuery(query);
            response = postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
        }
        return response;
    }

    public MultiplePostsResponse getPostsDataByDate(int offset, int limit, String dateStr) {
        log.debug("enter getPostsDataByDate(): input offset: {}, limit: {}, dateStr: {}", offset, limit, dateStr);

        final String INPUT_DATE_FORMAT = "yyyy-MM-dd";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(INPUT_DATE_FORMAT);
        dateFormatter.setLenient(false);
        Date date;
        try {
            date = dateFormatter.parse(dateStr);
        } catch (ParseException ex) {
            throw new RequestParamDateParseException("Input param date is invalid");
        }

        Pageable pageRequest = getPageRequest(offset, limit, PostSortMode.RECENT);
        List<Object[]> postsDataList = postRepository.findPostsByDate(date, pageRequest);
        long totalPostsCount = postRepository.getTotalPostsCountByDate(date);

        return postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
    }

    public MultiplePostsResponse getPostsDataByTag(int offset, int limit, String tag) {
        log.debug("enter getPostsDataByTag()");

        Pageable pageRequest = getPageRequest(offset, limit, PostSortMode.RECENT);
        List<Object[]> postsDataList = postRepository.findPostsByTag(tag, pageRequest);
        long totalPostsCount = postRepository.getTotalPostsCountByTag(tag);

        return postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
    }

    public CalendarPostsResponse getCalendarDataByYear(Integer year) {
        log.debug("enter getCalendarDataByYear()");

        if (year == null) {
            year = CURRENT_YEAR;
        }
        List<Integer> calendarYears = postRepository.findYears();
        List<Object[]> calendarPostsData = postRepository.findPostsCountPerDateByYear(year);

        return postsResponseMapper.calendarPostsResponse(calendarYears, calendarPostsData) ;
    }

    public synchronized SinglePostResponse getPostDataById(long id) {
        log.debug("enter getSinglePostById()");

        // ToDo count if authenticated user is moderator or author
        postRepository.updatePostViewCount(id);

        Object[] postData = getSinglePostData(id);
        final int POST_INDEX = 0;
        final int LIKECOUNT_INDEX = 2;
        final int DISLIKECOUNT_INDEX = 3;
        Post post = (Post)(postData[POST_INDEX]);
        long likeCount = (Long)(postData[LIKECOUNT_INDEX]);
        long dislikeCount = (Long)(postData[DISLIKECOUNT_INDEX]);

        initializeComments(post);
        initializeTags(post);

        return postsResponseMapper.singlePostResponse(post, likeCount, dislikeCount);
    }

    private Object[] getSinglePostData(long id) {
        List<Object[]> postDataList = postRepository.findPostById(id);
        if (postDataList == null || postDataList.isEmpty()) {
            throw new PostNotFoundException("Post not found");
        }
        Object[] postData = postDataList.get(0);
        Objects.requireNonNull(postData, "postData is null");
        final int SINGLE_POSTDATA_SIZE = 4;
        if ( postData.length != SINGLE_POSTDATA_SIZE) {
            throw new IllegalArgumentException("size of postData is not 4 ");
        }
        return postData;
    }

    private void initializeComments(Post post) {
        if (post.getComments() != null) {
            post.getComments().size();
        }
    }

    private void initializeTags(Post post) {
        if (post.getTags() != null) {
            post.getTags().size();
        }
    }


    public void test() {
        PageRequest p = PageRequest.of(0, 10);
        Page<Post> pp = postRepository.findPostsOrderByLikes(p);
        System.out.println(pp.getContent().get(0).getComments().size());

    }
}
