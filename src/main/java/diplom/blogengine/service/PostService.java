package diplom.blogengine.service;

import diplom.blogengine.api.request.PostCommentDataRequest;
import diplom.blogengine.api.request.PostDataRequest;
import diplom.blogengine.api.request.PostModerationRequest;
import diplom.blogengine.api.request.VoteDataRequest;
import diplom.blogengine.api.response.*;
import diplom.blogengine.api.response.mapper.PostsResponseMapper;
import diplom.blogengine.api.response.mapper.ResultResponseMapper;
import diplom.blogengine.exception.*;
import diplom.blogengine.model.*;
import diplom.blogengine.model.dto.CommentDto;
import diplom.blogengine.model.dto.PostDto;
import diplom.blogengine.model.dto.PostDtoExt;
import diplom.blogengine.model.dto.TagDto;
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
import java.util.stream.Stream;

@Slf4j
@Service
public class PostService implements IPostService {
    private final PostRepository postRepository;
    private final CachedPostRepository cachedPostRepository;
    private final PostsCounterStorage postsCounterStorage;
    private final TagRepository tagRepository;
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
                       TagRepository tagRepository,
                       ITagsService tagsService,
                       PostVoteRepository postVoteRepository,
                       PostsCounterStorage postsCounterStorage,
                       TimestampHelper timestampHelper,
                       ContentHelper contentHelper,
                       PostsResponseMapper postsResponseMapper,
                       ResultResponseMapper resultResponseMapper,
                       MessageSource messageSource) {
        this.postRepository = postRepository;
        this.cachedPostRepository = cachedPostRepository;
        this.postsCounterStorage = postsCounterStorage;
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
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

        List<PostDto> postsDtoList;
        long totalPostsCount;
        Pageable pageRequest = getPageRequestWithSort(offset, limit, mode);
        if (mode == PostSortMode.BEST) {
            postsDtoList = cachedPostRepository.findPostsDataOrderByLikesCount(pageRequest);
            totalPostsCount = cachedPostRepository.getTotalPostsCountExcludeDislikes();
        } else if (mode == PostSortMode.POPULAR) {
            postsDtoList = cachedPostRepository.findPostsDataOrderByCommentsCount(pageRequest);
            totalPostsCount = cachedPostRepository.getTotalPostsCount();
        } else  {
            postsDtoList = cachedPostRepository.findPostsData(pageRequest);
            totalPostsCount = cachedPostRepository.getTotalPostsCount();
        }

        return postsResponseMapper.multiplePostsResponse(convertToResponses(postsDtoList), totalPostsCount);
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
        List<PostDto> postsDtoList;
        long totalPostsCount;
        log.debug("getMyPostsData(): userId: {}, isActive: {}, moderationStatus: {}", authUserId, myPostStatus.isActiveFlag(), moderationStatus );

        if (myPostStatus.isActiveFlag()) {
            postsDtoList = postRepository.findMyPostsData(pageRequest, authUserId, moderationStatus);
            totalPostsCount = postRepository.getTotalMyPostsCount(authUserId, moderationStatus.toString());
        } else {
            postsDtoList = postRepository.findMyPostsNotActiveData(pageRequest, authUserId);
            totalPostsCount = postRepository.getTotalMyPostsNotActiveCount(authUserId);
        }
        return postsResponseMapper.multiplePostsResponse(convertToResponses(postsDtoList), totalPostsCount);
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
            List<PostDto> postsDtoList = cachedPostRepository.findPostsByQuery(query, pageRequest);
            long totalPostsCount = cachedPostRepository.getTotalPostsCountByQuery(query);
            response = postsResponseMapper.multiplePostsResponse(convertToResponses(postsDtoList), totalPostsCount);
        }
        return response;
    }

    @Override
    public MultiplePostsResponse getPostsDataByDate(int offset, int limit, String dateStr) {
        log.debug("enter getPostsDataByDate(): input offset: {}, limit: {}, dateStr: {}", offset, limit, dateStr);

        Date date;
        try {
            date = java.sql.Date.valueOf(LocalDate.parse(dateStr, formatter));
        } catch (DateTimeParseException ex) {
            throw new RequestParamDateParseException("Input param date is invalid");
        }

        Pageable pageRequest = getPageRequestWithSort(offset, limit, PostSortMode.RECENT);
        List<PostDto> postsDtoList = cachedPostRepository.findPostsByDate(date, pageRequest);
        long totalPostsCount = cachedPostRepository.getTotalPostsCountByDate(date);

        return postsResponseMapper.multiplePostsResponse(convertToResponses(postsDtoList), totalPostsCount);
    }

    @Override
    public MultiplePostsResponse getPostsDataByTag(int offset, int limit, String tag) {
        log.debug("enter getPostsDataByTag()");

        Pageable pageRequest = getPageRequestWithSort(offset, limit, PostSortMode.RECENT);
        List<PostDto> postsDtoList = cachedPostRepository.findPostsByTag(tag, pageRequest);
        long totalPostsCount = cachedPostRepository.getTotalPostsCountByTag(tag);

        return postsResponseMapper.multiplePostsResponse(convertToResponses(postsDtoList), totalPostsCount);
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
    public MultiplePostsResponse getModerationPostsData(int offset, int limit,
                                                        ModerationStatus status,
                                                        UserDetailsExt authUser) {
        log.debug("enter getModerationPostsData(): {}, {}, {}", offset, limit, status);

        Objects.requireNonNull(status, "moderationStatus is null");
        Objects.requireNonNull(status, "authUser is null");

        long authUserId = authUser.getId();
        Pageable pageRequest = getPageRequest(offset, limit);
        List<PostDto> postsDtoList = postRepository.findModerationPostsData(pageRequest, authUserId, status, status.toString());
        log.debug("postsDataList.size: {}", postsDtoList.size());

        long totalPostsCount = postRepository.getTotalModerationPostsCount(authUserId, status.toString()).orElse(0L);
        log.debug("totalPostsCount: {}", totalPostsCount);

        return postsResponseMapper.multiplePostsResponse(convertToResponses(postsDtoList), totalPostsCount);
    }

    private Stream<PostResponse> convertToResponses(List<PostDto> postsDtoList) {
        return postsDtoList.stream().map(this::postDtoToResponse);
    }

    private PostResponse postDtoToResponse(PostDto postDto) {
        log.debug("enter postDataToResponse()");

        Objects.requireNonNull(postDto, "input postData is null");

        long id = postDto.getId();
        int viewCount = postsCounterStorage.getOrUpdate(id, postDto.getViewCount());
        long timestamp = timestampHelper.toTimestampAtServerZone(Objects.requireNonNull(postDto.getTime()));
        String title = Objects.requireNonNull(postDto.getTitle());
        String announce = Objects.requireNonNull(postDto.getAnnounce());
        long likeCount = postDto.getLikeCount();
        long dislikeCount = postDto.getDislikeCount();
        long commentCount = postDto.getCommentCount();

        long userId = postDto.getUserId();
        String userName = Objects.requireNonNull(postDto.getUserName());
        UserInfoResponse userInfoResponse = new UserInfoResponse(userId, userName);

        log.debug("postDataToResponse(): id: {}, likeCount: {}, dislikeCount: {}, commentCount: {}", id, likeCount, dislikeCount, commentCount);

        return PostResponse.builder()
                .id(id)
                .timestamp(timestamp)
                .user(userInfoResponse)
                .title(title)
                .announce(announce)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .commentCount(commentCount)
                .viewCount(viewCount)
                .build();
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

        PostDtoExt postDto = cachedPostRepository.findPostById(postId, authUserId, authUserIsModerator);
        if (postDto == null) {
            throw new PostNotFoundException(postId);
        }
        long id = postDto.getId();

        List<CommentDto> comments = commentRepository.findByPostId(id);
        log.debug("getSinglePostById() comments: {}", comments.toString());
        Set<TagDto> tags = tagRepository.findByPostId(id);
        log.debug("getSinglePostById() tags: {}", tags.toString());

        if (authUserId == 0 || (!authUserIsModerator && authUserId != postDto.getUserId())) {
            int viewCount = updatePostViewCount(id);
            postDto.setViewCount(viewCount);
        }
        return postsResponseMapper.singlePostResponse(postDto, comments, tags);
    }

    private int updatePostViewCount(long postId) {
        log.debug("enter updatePostViewCount()");
        //postRepository.updatePostViewCount(postId);
        return postsCounterStorage.incrementAndGet(postId);
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
            throw new ValidationException(errors);
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
        System.out.println(Hibernate.isInitialized(updatedPost.getUser()));
        long authorId = updatedPost.getUser().getId();
        if (!authUser.isModerator() && authUser.getId() != authorId ) {
            throw new PostAccessDeniedException();
        }


        clearTags(postDataRequest);
        Map<String, String> errors = validatePostData(postDataRequest, locale);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Post post = convertDtoToPost(postDataRequest);
        post.setId(id);
        post.setUser(updatedPost.getUser());
        post.setViewCount(updatedPost.getViewCount());
        User moderator = updatedPost.getModerator();
        System.out.println(Hibernate.isInitialized(moderator));
        if (moderator != null) {
            post.setModerator(updatedPost.getModerator());
        }
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
            Long ownerPostId = commentRepository.findPostIdByCommentId(parentId).orElseThrow(() ->
                    new InputParameterException("comment.notFound", "parentId", parentId));

            if (ownerPostId != postId) {
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
