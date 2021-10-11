package diplom.blogengine.service;

import diplom.blogengine.api.response.CalendarPostsResponse;
import diplom.blogengine.api.response.MultiplePostsResponse;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.api.response.mapper.PostsResponseMapper;
import diplom.blogengine.exception.PostNotFoundException;
import diplom.blogengine.exception.RequestParamDateParseException;
import diplom.blogengine.model.ModerationStatus;
import diplom.blogengine.model.Post;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.security.AuthenticationService;
import diplom.blogengine.security.UserDetailsExt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
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
    private final AuthenticationService authService;
    private final int CURRENT_YEAR = LocalDate.now().getYear();

    public PostService(PostRepository postRepository, PostsResponseMapper postsResponseMapper, AuthenticationService authService) {
        this.postRepository = postRepository;
        this.postsResponseMapper = postsResponseMapper;
        this.authService = authService;
    }

    @Override
    public MultiplePostsResponse getPostsData(int offset, int limit, PostSortMode mode) {
        log.debug("enter getPostsData()");

        List<Object[]> postsDataList;
        long totalPostsCount;
        Pageable pageRequest = getPageRequestWithSort(offset, limit, mode);
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


    @Override
    public MultiplePostsResponse getMyPostsData(int offset, int limit, MyPostStatus myPostStatus) {
        log.debug("enter getMyPostsData(): {}, {}, {}", offset, limit, myPostStatus);

        long authUserId = authService.getAuthenticatedUserId();
        if (authUserId == 0) {
            return postsResponseMapper.emptyMultiplePostsResponse();
        }
        Pageable pageRequest = getPageRequest(offset, limit);
        ModerationStatus moderationStatus = myPostStatus.getModerationStatus();
        List<Object[]> postsDataList;
        long totalPostsCount;

        log.debug("getMyPostsData(): userId: {}, isActive: {}, moderationStatus: {}", authUserId, myPostStatus.isActiveFlag(), moderationStatus );

        if (myPostStatus.isActiveFlag()) {
            postsDataList = postRepository.findMyPostsData(pageRequest, authUserId, moderationStatus);
            totalPostsCount = postRepository.getTotalMyPostsCount(authUserId, moderationStatus);
        } else {
            postsDataList = postRepository.findMyPostsNotActiveData(pageRequest, authUserId);
            totalPostsCount = postRepository.getTotalMyPostsNotActiveCount(authUserId);
        }
        return postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
    }

    private Pageable getPageRequestWithSort(int offset, int limit, PostSortMode mode) {
        log.debug("enter getPageRequest()");

        if (mode == PostSortMode.BEST || mode == PostSortMode.POPULAR) {
            return getPageRequest(offset, limit);
        } else {
            final String SORTING_FIELD_TIME = "time";
            Sort.Direction direction = Sort.Direction.DESC;
            if (mode == PostSortMode.EARLY) {
                direction = Sort.Direction.ASC;
            }
            return PageRequest.of(offset, limit, Sort.by(direction, SORTING_FIELD_TIME));
        }
    }

    private Pageable getPageRequest(int offset, int limit) {
        return PageRequest.of(offset, limit);
    }

    public MultiplePostsResponse getPostsDataByQuery(int offset, int limit, String query) {
        log.debug("enter getPostsDataByQuery()");

        MultiplePostsResponse response = null;
        if (query == null || query.isEmpty()) {
            response = getPostsData(offset, limit, PostSortMode.RECENT);
        } else {
            Pageable pageRequestWithSort = getPageRequestWithSort(offset, limit, PostSortMode.RECENT);
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

        Pageable pageRequest = getPageRequestWithSort(offset, limit, PostSortMode.RECENT);
        List<Object[]> postsDataList = postRepository.findPostsByDate(date, pageRequest);
        long totalPostsCount = postRepository.getTotalPostsCountByDate(date);

        return postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
    }

    public MultiplePostsResponse getPostsDataByTag(int offset, int limit, String tag) {
        log.debug("enter getPostsDataByTag()");

        Pageable pageRequest = getPageRequestWithSort(offset, limit, PostSortMode.RECENT);
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

    public SinglePostResponse getPostDataById(long postId) {
        log.debug("enter getSinglePostById()");

        long authUserId = 0;
        boolean authUserIsModerator = false;
        UserDetailsExt authUser = authService.getAuthenticatedUser();
        if (authUser != null) {
            authUserId = authUser.getId();
            authUserIsModerator = authUser.isModerator();
        }

        Object[] postData = getSinglePostData(postId, authUserId, authUserIsModerator);
        final int POST_INDEX = 0;
        final int LIKECOUNT_INDEX = 2;
        final int DISLIKECOUNT_INDEX = 3;
        Post post = (Post)(postData[POST_INDEX]);
        long likeCount = (Long)(postData[LIKECOUNT_INDEX]);
        long dislikeCount = (Long)(postData[DISLIKECOUNT_INDEX]);

        initializeComments(post);
        initializeTags(post);

        if (authUserId == 0 || (!authUserIsModerator && authUserId != post.getUser().getId())) {
            updatePostViewCount(post);
        }
        return postsResponseMapper.singlePostResponse(post, likeCount, dislikeCount);
    }

    private Object[] getSinglePostData(long postId, long authUserId, boolean authUserIsModerator) {
        log.debug("enter getSinglePostData()");

        List<Object[]> postDataList = postRepository.findPostById(postId, authUserId, authUserIsModerator);
        if (postDataList == null || postDataList.isEmpty()) {
            throw new PostNotFoundException("Post " + postId + " not found");
        }
        Object[] postData = postDataList.get(0);
        Objects.requireNonNull(postData, "postData is null");
        final int SINGLE_POSTDATA_SIZE = 4;
        if ( postData.length != SINGLE_POSTDATA_SIZE) {
            throw new IllegalArgumentException("size of postData is not 4 ");
        }
        return postData;
    }

    private void updatePostViewCount(Post post) {
        log.debug("enter getSinglePostById()");
        postRepository.updatePostViewCount(post.getId());
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


    @Autowired
    private EntityManager em;
    public void test() {
        PageRequest p = PageRequest.of(0, 10);

        // post , post.user, post.comments выбираются тремя запросами select:
        //Page<Post> pp = postRepository.findPostsOrderByLikes(p);
        //pp.getContent().get(0).getComments().size();



        // post и post.user выбираются двумя запросами select: с Join fetch не компиллируется
        //Page<Post> pp = postRepository.findPostTest(p);

        // post и post.user выбираются двумя запросами select:
        //Post post = em.createQuery("SELECT p FROM Post p WHERE p.id = 1", Post.class).getSingleResult();

        // post и post.user выбираются двумя запросами select:
        // Post post = em.createQuery("SELECT p FROM Post p JOIN p.user WHERE p.id = 1", Post.class).getSingleResult();

        // post и post.user выбираются одним запросом select (Post.user fetchType=Eager):
        Post post  = em.find(Post.class, 1L);

        // post и post.user выбираются одним запросом select (Post.user fetchType=Eager):
        //Post post = em.createQuery("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = 1", Post.class).getSingleResult();

        // post и post.user, post.comments, votes, tags выбираются одним запросом select (для всех ассоциаций в Post стоит fetchType=Eager, тип Set):
        //Post post  = em.find(Post.class, 1L);

    }
}
