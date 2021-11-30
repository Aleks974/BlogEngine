package diplom.blogengine.service;

import diplom.blogengine.api.request.PostCommentDataRequest;
import diplom.blogengine.api.request.PostDataRequest;
import diplom.blogengine.api.request.PostModerationRequest;
import diplom.blogengine.api.request.VoteDataRequest;
import diplom.blogengine.api.response.CalendarPostsResponse;
import diplom.blogengine.api.response.MultiplePostsResponse;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.api.response.mapper.PostsResponseMapper;
import diplom.blogengine.api.response.mapper.ResultResponseMapper;
import diplom.blogengine.exception.*;
import diplom.blogengine.model.*;
import diplom.blogengine.repository.*;
import diplom.blogengine.security.UserDetailsExt;
import diplom.blogengine.service.util.ContentHelper;
import diplom.blogengine.service.util.TimestampHelper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private final CachedPostRepository cachedPostRepository;
    private final PostCommentRepository commentRepository;
    private final ITagsService tagsService;
    private final PostVoteRepository postVoteRepository;
    private final PostsResponseMapper postsResponseMapper;
    private final ResultResponseMapper resultResponseMapper;
    private final TimestampHelper timestampHelper;
    private final ContentHelper contentHelper;
    private final MessageSource messageSource;

    @PersistenceContext
    private EntityManager entityManager;

    private final int CURRENT_YEAR = LocalDate.now().getYear();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String ANNOUNCE_MORE = "...";
    private static final int POST_ANNOUNCE_LENGTH = 150;
    private static final int TITLE_MIN_LENGTH = 3;
    private static final int TITLE_MAX_LENGTH = 255;
    private static final int TEXT_MIN_LENGTH = 50;
    private static final int TEXT_MAX_LENGTH = 65000;
    private static final int COMMENT_TEXT_MIN_LENGTH = 3;
    private static final int COMMENT_TEXT_MAX_LENGTH = 65000;
    private static final Pattern TAG_PATTERN = Pattern.compile("^[\\wа-яА-Я\\s\\.]{2,}$");

    public PostService(PostRepository postRepository,
                       CachedPostRepository cachedPostRepository,
                       PostCommentRepository commentRepository,
                       ITagsService tagsService,
                       PostVoteRepository postVoteRepository,
                       TimestampHelper timestampHelper,
                       ContentHelper contentHelper,
                       PostsResponseMapper postsResponseMapper,
                       ResultResponseMapper resultResponseMapper,
                       MessageSource messageSource) {
        this.postRepository = postRepository;
        this.cachedPostRepository = cachedPostRepository;
        this.commentRepository = commentRepository;
        this.tagsService = tagsService;
        this.postVoteRepository = postVoteRepository;
        this.timestampHelper = timestampHelper;
        this.contentHelper = contentHelper;
        this.postsResponseMapper = postsResponseMapper;
        this.resultResponseMapper = resultResponseMapper;
        this.messageSource = messageSource;
    }

    @Override
    public MultiplePostsResponse getPostsData(int offset, int limit, PostSortMode mode) {
        log.debug("enter getPostsData(), offset: {}, limit: {}, mode: {}", offset, limit, mode);

        List<Object[]> postsDataList;
        long totalPostsCount;
        Pageable pageRequest = getPageRequestWithSort(offset, limit, mode);
        if (mode == PostSortMode.BEST) {
            postsDataList = cachedPostRepository.findPostsDataOrderByLikesCount(pageRequest);
            totalPostsCount = cachedPostRepository.getTotalPostsCountExcludeDislikes();
        } else if (mode == PostSortMode.POPULAR) {
            postsDataList = cachedPostRepository.findPostsDataOrderByCommentsCount(pageRequest);
            totalPostsCount = cachedPostRepository.getTotalPostsCount();
        } else  {
            postsDataList = cachedPostRepository.findPostsData(pageRequest);
            totalPostsCount = cachedPostRepository.getTotalPostsCount();
        }

        return postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
    }


    @Override
    public MultiplePostsResponse getMyPostsData(int offset, int limit, MyPostStatus myPostStatus, UserDetailsExt authUser) {
        log.debug("enter getMyPostsData(): {}, {}, {}", offset, limit, myPostStatus);

        Objects.requireNonNull(myPostStatus, "myPostStatus is null");
        Objects.requireNonNull(authUser, "authUser is null");

        long authUserId = authUser.getId();
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
            totalPostsCount = postRepository.getTotalMyPostsCount(authUserId, moderationStatus.toString());
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

    @Override
    public MultiplePostsResponse getPostsDataByQuery(int offset, int limit, String query) {
        log.debug("enter getPostsDataByQuery()");

        MultiplePostsResponse response = null;
        if (query == null || query.isEmpty()) {
            response = getPostsData(offset, limit, PostSortMode.RECENT);
        } else {
            query = query.toLowerCase();
            Pageable pageRequest = getPageRequestWithSort(offset, limit, PostSortMode.RECENT);
            List<Object[]> postsDataList = cachedPostRepository.findPostsByQuery(query, pageRequest);
            long totalPostsCount = cachedPostRepository.getTotalPostsCountByQuery(query);
            response = postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
        }
        return response;
    }

    @Override
    public MultiplePostsResponse getPostsDataByDate(int offset, int limit, String dateStr) {
        log.debug("enter getPostsDataByDate(): input offset: {}, limit: {}, dateStr: {}", offset, limit, dateStr);

       /* final String INPUT_DATE_FORMAT = "yyyy-MM-dd";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(INPUT_DATE_FORMAT);
        dateFormatter.setLenient(false);*/
        Date date;
        try {
            date = java.sql.Date.valueOf(LocalDate.parse(dateStr, formatter));
        } catch (DateTimeParseException ex) {
            throw new RequestParamDateParseException("Input param date is invalid");
        }

        Pageable pageRequest = getPageRequestWithSort(offset, limit, PostSortMode.RECENT);
        List<Object[]> postsDataList = cachedPostRepository.findPostsByDate(date, pageRequest);
        long totalPostsCount = cachedPostRepository.getTotalPostsCountByDate(date);

        return postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
    }

    @Override
    public MultiplePostsResponse getPostsDataByTag(int offset, int limit, String tag) {
        log.debug("enter getPostsDataByTag()");

        Pageable pageRequest = getPageRequestWithSort(offset, limit, PostSortMode.RECENT);
        List<Object[]> postsDataList = cachedPostRepository.findPostsByTag(tag, pageRequest);
        long totalPostsCount = cachedPostRepository.getTotalPostsCountByTag(tag);

        return postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
    }

    @Override
    public CalendarPostsResponse getCalendarDataByYear(Integer year) {
        log.debug("enter getCalendarDataByYear()");

        if (year == null) {
            year = CURRENT_YEAR;
        }
        List<Integer> calendarYears = postRepository.findYears();
        List<Object[]> calendarPostsData = postRepository.findPostsCountPerDateByYear(year);

        return postsResponseMapper.calendarPostsResponse(calendarYears, calendarPostsData) ;
    }

    @Override
    public SinglePostResponse getPostDataById(long postId, UserDetailsExt authUser) {
        log.debug("enter getSinglePostById()");

        long authUserId = 0;
        boolean authUserIsModerator = false;
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

        List<Object[]> postDataList = cachedPostRepository.findPostById(postId, authUserId, authUserIsModerator);
        if (postDataList == null || postDataList.isEmpty()) {
            throw new PostNotFoundException(postId);
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

    //ToDo ?
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

    @Override
    public MultiplePostsResponse getModerationPostsData(int offset, int limit,
                                                        ModerationStatus status,
                                                        UserDetailsExt authUser) {
        log.debug("enter getModerationPostsData(): {}, {}, {}", offset, limit, status);

        Objects.requireNonNull(status, "moderationStatus is null");
        Objects.requireNonNull(status, "authUser is null");

        long authUserId = authUser.getId();
        Pageable pageRequest = getPageRequest(offset, limit);
        List<Object[]> postsDataList = postRepository.findModerationPostsData(pageRequest, authUserId, status, status.toString());
        log.debug("postsDataList.size: {}", postsDataList.size());

        long totalPostsCount = postRepository.getTotalModerationPostsCount(authUserId, status.toString()).orElse(0L);
        log.debug("totalPostsCount: {}", totalPostsCount);

        return postsResponseMapper.multiplePostsResponse(postsDataList, totalPostsCount);
    }

    @Override
    public ResultResponse newPost(PostDataRequest postDataRequest, UserDetailsExt authUser, boolean moderationIsEnabled, Locale locale) {
        log.debug("enter newPost()");

        Objects.requireNonNull(postDataRequest, "postDataRequest is null");
        Objects.requireNonNull(authUser, "authUser is null");
        Objects.requireNonNull(locale, "locale is null");

        clearTags(postDataRequest);
        Map<String, String> errors = validatePostData(postDataRequest, locale);
        if (!errors.isEmpty()) {
            log.debug("newPost(): validation errors: {}", errors.toString());
            throw new ValidationException(errors);
            //return resultResponseMapper.failure(errors);
        }

        Post post = convertDtoToPost(postDataRequest);
        User user = entityManager.getReference(User.class, authUser.getId());
        post.setUser(user);

        ModerationStatus moderationStatus;
        if (!moderationIsEnabled || authUser.isModerator()) {
            moderationStatus = ModerationStatus.ACCEPTED;
        } else {
            moderationStatus = ModerationStatus.NEW;
        }
        post.setModerationStatus(moderationStatus);

        System.out.println(Hibernate.isInitialized(post));
        System.out.println(Hibernate.isInitialized(user));
        postRepository.save(post);

        cachedPostRepository.clearMultiplePostsAndCountsCache();

        return resultResponseMapper.success();
    }

    @Modifying
    @Transactional
    @Override
    public ResultResponse updatePost(long id, PostDataRequest postDataRequest, UserDetailsExt authUser,
                                     boolean moderationIsEnabled, Locale locale) {
        log.debug("enter updatePost(): id: {}", id);

        Objects.requireNonNull(postDataRequest, "postDataRequest is null");
        Objects.requireNonNull(authUser, "authUser is null");
        Objects.requireNonNull(locale, "locale is null");

        // ToDo проверить статус attached или detached
        // user, Moderator инициализирован?
        Post updatedPost = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        long authorId = updatedPost.getUser().getId();
        if (!authUser.isModerator() && authUser.getId() != authorId ) {
            throw new PostAccessDeniedException();
        }

        clearTags(postDataRequest);
        Map<String, String> errors = validatePostData(postDataRequest, locale);
        if (!errors.isEmpty()) {
            log.debug("newPost(): validation errors: {}", errors.toString());
            throw new ValidationException(errors);
            //return resultResponseMapper.failure(errors);
        }

        Post post = convertDtoToPost(postDataRequest);
        post.setId(id);
        post.setUser(updatedPost.getUser());
        post.setViewCount(updatedPost.getViewCount());
        post.setModerator(updatedPost.getModerator());
        if (moderationIsEnabled && !authUser.isModerator()) {
            post.setModerationStatus(ModerationStatus.NEW);
        } else {
            post.setModerationStatus(updatedPost.getModerationStatus());
        }

        postRepository.save(post);

        cachedPostRepository.clearSinglePostCache(id);
        cachedPostRepository.clearMultiplePostsAndCountsCache();

        return resultResponseMapper.success();
    }

    private void clearTags(PostDataRequest postDataRequest) {
        String title = contentHelper.clearAllTags(postDataRequest.getTitle());
        postDataRequest.setTitle(contentHelper.clearExtraSpaces(title));
        String text = contentHelper.clearAllTagsExceptPermitted(postDataRequest.getText());
        postDataRequest.setText(contentHelper.clearExtraSpaces(text));
    }

    private void clearTags(PostCommentDataRequest commentDataRequest) {
        String text = contentHelper.clearAllTagsExceptPermitted(commentDataRequest.getText());
        commentDataRequest.setText(contentHelper.clearExtraSpaces(text));
    }

    private Map<String, String> validatePostData(PostDataRequest postDataRequest, Locale locale) {
        log.debug("enter validatePostData()");

        Map<String, String> errors = new HashMap<>();
        errors.putAll(validateText(postDataRequest.getTitle(), "title", locale, TITLE_MIN_LENGTH, TITLE_MAX_LENGTH));
        errors.putAll(validateText(postDataRequest.getText(), "text", locale, TEXT_MIN_LENGTH, TEXT_MAX_LENGTH));
        if (postDataRequest.getTags() != null) {
            errors.putAll(validateTags(postDataRequest.getTags(), locale));
        }
        return errors;
    }

    private Map<String, String> validateText(String content, String fieldName, Locale locale, int min, int max) {
        Map<String, String> errors = new HashMap<>();
        if (content.isBlank()) {
            errors.put(fieldName, messageSource.getMessage(fieldName.concat(".isblank"), null, locale));
        } else if (content.length() < min) {
            errors.put(fieldName, messageSource.getMessage(fieldName.concat(".isshort"), null, locale));
        } else if (content.length() > max) {
            errors.put(fieldName, messageSource.getMessage(fieldName.concat(".islong"), null, locale));
        }
        return errors;
    }

    private Map<String, String> validateTags(Set<String> tags, Locale locale) {
        Map<String, String> errors = new HashMap<>();
        for (String tag : tags) {
            if (!TAG_PATTERN.matcher(tag).matches()) {
                errors.put("tags", messageSource.getMessage("tag.isincorrect", null, locale));
            }
        }
        return errors;
    }

    private Post convertDtoToPost(PostDataRequest postDataRequest) {
        log.debug("enter convertDtoToPost()");

        Post post = new Post();

        long currentTimestamp =  timestampHelper.genCurrentTimestamp();
        long timestamp = postDataRequest.getTimestamp();
        if (timestamp < currentTimestamp ) {
            timestamp = currentTimestamp;
        }
        post.setTime(timestampHelper.toLocalDateTimeAtServerZone(timestamp));

        final int ACTIVE_VAL = 1;
        post.setActive(postDataRequest.getActive() == ACTIVE_VAL);

        post.setTitle(postDataRequest.getTitle());

        Set<Tag> tags = null;
        if (postDataRequest.getTags() != null) {
            tags = postDataRequest.getTags().stream()
                    .map((String t) -> tagsService.getOrSaveNewTag(t))
                    .collect(Collectors.toSet());
        }
        post.setTags(tags);

        post.setText(postDataRequest.getText());

        // announce
        String clearedText = contentHelper.clearAllTags(post.getText());
        String announce = clearedText.length() > POST_ANNOUNCE_LENGTH ?
                clearedText.substring(0, POST_ANNOUNCE_LENGTH).concat(ANNOUNCE_MORE) : clearedText;
        announce = contentHelper.clearExtraSpaces(announce);
        post.setAnnounce(announce);

        return post;
    }

    @Override
    public ResultResponse newComment(PostCommentDataRequest commentDataRequest, UserDetailsExt authUser, Locale locale) {
        log.debug("enter newComment()");

        Objects.requireNonNull(commentDataRequest);
        Objects.requireNonNull(locale);
        Objects.requireNonNull(authUser);

        long postId = commentDataRequest.getPostId();
        long parentId = commentDataRequest.getParentId();
        validateCommentPostAndParent(postId, parentId);

        clearTags(commentDataRequest);
        Map<String, String> errors = validateText(commentDataRequest.getText(),
                                    "commentText", locale, COMMENT_TEXT_MIN_LENGTH, COMMENT_TEXT_MAX_LENGTH);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
            //return resultResponseMapper.failure(errors);
        }

        PostComment comment = convertDtoToComment(commentDataRequest);
        if (parentId > 0) {
            PostComment parentComment = entityManager.getReference(PostComment.class, parentId);
            comment.setParent(parentComment);
        }
        Post post = entityManager.getReference(Post.class, postId);
        comment.setPost(post);

        User user = entityManager.getReference(User.class, authUser.getId());
        comment.setUser(user);

        comment = commentRepository.save(comment);

        return resultResponseMapper.success(comment.getId());
    }

    private void validateCommentPostAndParent(long postId, long parentId) {
        if (postId <= 0) {
            throw new InputParameterException("commentPostId.positive", "postId", postId);
        }
        postRepository.findPostId(postId).orElseThrow(() -> new InputParameterException("post.notFound", "postId", postId));

        if (parentId < 0) {
            throw new InputParameterException("commentParentId.positive", "parentId", parentId);

        } else if (parentId > 0) {
            Long parentCommentPostId = commentRepository.findPostIdByCommentId(parentId).orElseThrow(() ->
                    new InputParameterException("comment.notFound", "parentId", parentId));

            if (parentCommentPostId != postId) {
                throw new InputParameterException("comment.illegalParams");
            }
        }
    }

    private PostComment convertDtoToComment(PostCommentDataRequest commentDataRequest) {
        PostComment comment = new PostComment();
        comment.setTime(LocalDateTime.now());
        comment.setText(commentDataRequest.getText());
        return comment;
    }

    @Override
    public ResultResponse newVote(VoteParameter voteParam,
                                  VoteDataRequest voteDataRequest,
                                  UserDetailsExt authUser,
                                  Locale locale) {
        log.debug("enter newVote()");

        Objects.requireNonNull(voteDataRequest);
        Objects.requireNonNull(locale);
        Objects.requireNonNull(authUser);

        long postId = voteDataRequest.getPostId();
        postRepository.findPostId(postId).orElseThrow(() ->  new InputParameterException("post.notFound", "postId", postId));

        int value = voteParam.getValue();
        long authUserId = authUser.getId();
        PostVote postVote = postVoteRepository.findByPostAndUserIds(postId, authUserId);
        if (postVote == null) {
            postVote = new PostVote();
            postVote.setUser(entityManager.getReference(User.class, authUserId));
            postVote.setPost(entityManager.getReference(Post.class, postId));
        } else if (postVote.getValue() == value) {
            return resultResponseMapper.failure();
        }
        postVote.setValue(value);
        postVote.setTime(LocalDateTime.now());
        postVoteRepository.save(postVote);

        return resultResponseMapper.success();
    }

    @Override
    public ResultResponse moderatePost(PostModerationRequest postModerationRequest, UserDetailsExt authUser) {
        log.debug("enter moderatePost()");

        Objects.requireNonNull(postModerationRequest, "postModerationRequest is null");
        Objects.requireNonNull(authUser, "authUser is null");

        long postId = postModerationRequest.getPostId();
        Post postToModerate = postRepository.findActivePostById(postId).orElseThrow(() ->
                    new InputParameterException("post.notFound", "postId", postId));

        try {
            ModerationStatus newStatus = postModerationRequest.getDecision().getModerationStatus();
            ModerationStatus currentStatus = postToModerate.getModerationStatus();

            if (newStatus != currentStatus) {
                postToModerate.setModerationStatus(newStatus);
                User moderator = entityManager.getReference(User.class, authUser.getId());
                postToModerate.setModerator(moderator);
                // ToDo проверить tags не обнулится? как обновлять только нужные поля?
                postRepository.save(postToModerate);
            }
        } catch (Exception ex) {
            return resultResponseMapper.failure();
        }

        return resultResponseMapper.success();

    }


    //////////////////////////////////////////
 /*   @Autowired
    private EntityManager em;
    public void test() {
        //PageRequest p = PageRequest.of(0, 10);

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
       // Post post  = em.find(Post.class, 1L);

        // post и post.user выбираются одним запросом select (Post.user fetchType=Eager):
        //Post post = em.createQuery("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = 1", Post.class).getSingleResult();

        // post и post.user, post.comments, votes, tags выбираются одним запросом select (для всех ассоциаций в Post стоит fetchType=Eager, тип Set):
        //Post post  = em.find(Post.class, 1L);

    }*/
}
