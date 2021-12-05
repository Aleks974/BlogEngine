package diplom.blogengine.repository;

import diplom.blogengine.model.ModerationStatus;
import diplom.blogengine.model.Post;
import diplom.blogengine.model.dto.PostDto;
import diplom.blogengine.model.dto.PostDtoExt;
import diplom.blogengine.model.dto.StatPostsDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    static final String JPQL_SELECT_COUNT_POSTS = "SELECT COUNT(distinct p.id) " +
                                                  "FROM Post p ";
    static final String SELECT_COUNT_POSTS = "SELECT COUNT(distinct p.id) " +
                                             "FROM posts p ";
    //static final String JPQL_SELECT_POSTS_DATA = "SELECT p, u, COUNT(DISTINCT CASE WHEN (v.value = 1) THEN v.id ELSE NULL END) AS like_count, COUNT(DISTINCT CASE WHEN (v.value = -1) THEN v.id ELSE NULL END) AS dislike_count,  COUNT(DISTINCT c.id) AS comment_count " +
    static final String JPQL_SELECT_POSTS_DATA = "SELECT new diplom.blogengine.model.dto.PostDto(p.id, p.time, p.title, p.announce, COUNT(DISTINCT CASE WHEN (v.value = 1) THEN v.id ELSE NULL END) AS like_count, COUNT(DISTINCT CASE WHEN (v.value = -1) THEN v.id ELSE NULL END) AS dislike_count,  COUNT(DISTINCT c.id) AS comment_count, p.viewCount, u.id, u.name) " +
                                                 "FROM Post p " +
                                                 "JOIN p.user u " +
                                                 "LEFT JOIN p.votes v " +
                                                 "LEFT JOIN p.comments c ";
    static final String JPQL_SELECT_MODERATION_POSTS_DATA = JPQL_SELECT_POSTS_DATA +
                                                            "LEFT JOIN p.moderator m ";
    static final String JPQL_SELECT_STATISTICS_DATA = "SELECT new diplom.blogengine.model.dto.StatPostsDto(COUNT(distinct p.id), SUM(p.viewCount), MIN(p.time)) " +
                                                        "FROM Post p " +
                                                        "JOIN p.user u ";
    static final String JPQL_SELECT_SINGLE_POST_DATA = "SELECT new diplom.blogengine.model.dto.PostDtoExt(p.id, p.time, p.title, p.announce, COUNT(DISTINCT CASE WHEN (v.value = 1) THEN v.id ELSE NULL END) AS like_count, COUNT(DISTINCT CASE WHEN (v.value = -1) THEN v.id ELSE NULL END) AS dislike_count,  0L, p.viewCount, p.text, p.isActive, u.id, u.name) " +
                                                "FROM Post p " +
                                                "JOIN p.user u " +
                                                "LEFT JOIN p.votes v ";

    static final String JPQL_JOIN_P_TAGS = " JOIN p.tags t ";
    static final String JPQL_JOIN_P_VOTES = " LEFT JOIN p.votes v ";

    static final String JPQL_WHERE_GENERAL = "WHERE p.isActive = true AND p.moderationStatus = 'ACCEPTED' AND p.time <= NOW() ";
    static final String JPQL_WHERE_USERID_AND_NOTACTIVE = "WHERE u.id = :authUserId AND p.isActive = 0 ";
    static final String WHERE_USERID_AND_NOTACTIVE = "WHERE p.user_id = :authUserId AND p.is_active = 0 ";
    static final String JPQL_WHERE_USERID_AND_ACTIVE_AND_STATUS = "WHERE u.id = :authUserId AND p.isActive = 1 AND p.moderationStatus = :moderationStatus ";
    static final String WHERE_USERID_AND_ACTIVE_AND_STATUS = "WHERE p.user_id = :authUserId AND p.is_active = 1 AND p.moderation_status = :moderationStatus ";
    static final String JPQL_WHERE_ACTIVE_AND_STATUS_OR_MODERATORID_AND_STATUS = "WHERE p.isActive = true AND (:moderationStatusStr = 'NEW' OR m.id = :authUserId) AND p.moderationStatus = :moderationStatus ";
    static final String WHERE_ACTIVE_AND_STATUS_OR_MODERATORID_AND_STATUS = "WHERE p.is_active = 1 AND ((:moderationStatusStr = 'NEW' OR p.moderator_id = :authUserId) AND p.moderation_status = :moderationStatusStr) ";
    static final String WHERE_SEARCH_BY_PARAM_QUERY = " AND (p.title LIKE %:query% OR p.text LIKE %:query%) ";
    static final String WHERE_SEARCH_BY_PARAM_DATE = " AND DATE(p.time) = :date ";
    static final String WHERE_SEARCH_BY_PARAM_TAG = " AND t.name = :tag ";
    static final String WHERE_VOTES_EXCLUDE_DISLIKE = " AND (v.value = NULL OR v.value = 1) ";

    static final String GROUP_BY_P_ID = " GROUP BY p.id ";

    static final String HAVING_VOTES_LIKE = " HAVING col_4_0_ > 0 OR (col_4_0_ = 0 AND col_5_0_ = 0) ";
    //static final String HAVING_VOTES_LIKE = " HAVING like_count > 0 OR (like_count = 0 AND dislike_count = 0) ";

    static final String ORDER_BY_LIKE_COUNT = " ORDER BY like_count DESC ";
    static final String ORDER_BY_COMMENT_COUNT = " ORDER BY comment_count DESC ";
    static final String ORDER_BY_TIME = " ORDER BY p.time DESC ";


    @Query(JPQL_SELECT_POSTS_DATA +
           JPQL_WHERE_GENERAL +
           GROUP_BY_P_ID)
    List<PostDto> findPostsData(Pageable pageRequestWithSort);

    @Query(JPQL_SELECT_COUNT_POSTS +
           JPQL_WHERE_GENERAL)
    long getTotalPostsCount();


    @Query(JPQL_SELECT_POSTS_DATA +
            JPQL_WHERE_USERID_AND_NOTACTIVE +
            GROUP_BY_P_ID +
            ORDER_BY_TIME)
    List<PostDto> findMyPostsNotActiveData(Pageable pageRequest, @Param("authUserId") long authUserId);

    @Query(value = SELECT_COUNT_POSTS +
                   WHERE_USERID_AND_NOTACTIVE,
           nativeQuery = true)
    long getTotalMyPostsNotActiveCount(@Param("authUserId") long authUserId);


    @Query(JPQL_SELECT_POSTS_DATA +
            JPQL_WHERE_USERID_AND_ACTIVE_AND_STATUS +
            GROUP_BY_P_ID +
            ORDER_BY_TIME)
    List<PostDto> findMyPostsData(Pageable pageRequest,
                                    @Param("authUserId") long authUserId,
                                    @Param("moderationStatus") ModerationStatus moderationStatus);

    @Query(value = SELECT_COUNT_POSTS +
                   WHERE_USERID_AND_ACTIVE_AND_STATUS,
           nativeQuery = true)
    long getTotalMyPostsCount(@Param("authUserId") long authUserId, @Param("moderationStatus") String moderationStatus);


    @Query(JPQL_SELECT_POSTS_DATA +
           JPQL_WHERE_GENERAL +
           GROUP_BY_P_ID +
           HAVING_VOTES_LIKE +
           ORDER_BY_LIKE_COUNT)
    List<PostDto> findPostsDataOrderByLikesCount(Pageable pageRequest);

    @Query(JPQL_SELECT_COUNT_POSTS +
           JPQL_JOIN_P_VOTES +
           JPQL_WHERE_GENERAL +
           WHERE_VOTES_EXCLUDE_DISLIKE)
    long getTotalPostsCountExcludeDislikes();


    @Query( JPQL_SELECT_POSTS_DATA +
            JPQL_WHERE_GENERAL +
            GROUP_BY_P_ID +
            ORDER_BY_COMMENT_COUNT)
    List<PostDto> findPostsDataOrderByCommentsCount(Pageable pageRequest);


    @Query(JPQL_SELECT_POSTS_DATA +
           JPQL_WHERE_GENERAL + WHERE_SEARCH_BY_PARAM_QUERY +
           GROUP_BY_P_ID)
    List<PostDto> findPostsByQuery(@Param("query") String query, Pageable pageRequestWithSort);

    @Query(JPQL_SELECT_COUNT_POSTS +
            JPQL_WHERE_GENERAL + WHERE_SEARCH_BY_PARAM_QUERY)
    long getTotalPostsCountByQuery(@Param("query") String query);


    @Query(JPQL_SELECT_POSTS_DATA +
           JPQL_WHERE_GENERAL + WHERE_SEARCH_BY_PARAM_DATE +
           GROUP_BY_P_ID)
    List<PostDto> findPostsByDate(@Param("date") Date date, Pageable pageRequestWithSort);

    @Query(JPQL_SELECT_COUNT_POSTS +
            JPQL_WHERE_GENERAL + WHERE_SEARCH_BY_PARAM_DATE)
    long getTotalPostsCountByDate(@Param("date") Date date);


    @Query(JPQL_SELECT_POSTS_DATA +
           JPQL_JOIN_P_TAGS +
           JPQL_WHERE_GENERAL + WHERE_SEARCH_BY_PARAM_TAG +
           GROUP_BY_P_ID)
    List<PostDto> findPostsByTag(@Param("tag") String tag, Pageable pageRequestWithSort);

    @Query(JPQL_SELECT_COUNT_POSTS +
           JPQL_JOIN_P_TAGS +
           JPQL_WHERE_GENERAL + WHERE_SEARCH_BY_PARAM_TAG)
    long getTotalPostsCountByTag(@Param("tag") String tag);


    @Query("SELECT YEAR(p.time) AS posts_year " +
            "FROM Post p " +
            JPQL_WHERE_GENERAL +
            "GROUP BY posts_year " +
            "ORDER BY posts_year ASC")
    List<Integer> findYears();

    @Query("SELECT DATE(p.time) AS posts_date, COUNT(p.id) " +
            "FROM Post p " +
            JPQL_WHERE_GENERAL + " AND YEAR(p.time) = :year " +
            "GROUP BY posts_date " +
            "ORDER BY posts_date DESC")
    List<Object[]> findPostsCountPerDateByYear(@Param("year" ) Integer year);

    @Query(JPQL_SELECT_SINGLE_POST_DATA +
            "WHERE p.id = :id AND (:authUserIsModerator = TRUE OR :authUserId = u.id OR " +
            "(p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= NOW())) " +
            "GROUP BY p.id")
    PostDtoExt findPostById(@Param("id") long id, @Param("authUserId") long authUserId, @Param("authUserIsModerator") boolean authUserIsModerator);

    @Modifying
    @Transactional
    @Query(value = "UPDATE posts p SET p.view_count = p.view_count + 1 WHERE p.id = :id LIMIT 1"
                , nativeQuery = true)
    int updatePostViewCount(@Param("id") long id);

    @Query(value = "SELECT count(p.id) " +
                    "FROM posts p " +
                    "WHERE is_active = 1 AND moderation_status = 'NEW'",
            nativeQuery = true)
    long getModerationPostCount();


    @Query(value = "SELECT p.id " +
            "FROM posts p " +
            "WHERE p.id = :id AND p.is_active = 1 AND p.moderation_status = 'ACCEPTED' AND p.time <= NOW() " +
            "LIMIT 1", nativeQuery = true)
    Optional<Long> findPostId(@Param("id") long id);


    @EntityGraph(attributePaths = {"user", "moderator"})
    @Query(value = "SELECT p " +
            "FROM Post p " +
            "WHERE p.id = :id AND p.isActive = 1 ")
    Optional<Post> findActivePostById(@Param("id") long id);


    @Query(JPQL_SELECT_MODERATION_POSTS_DATA +
            JPQL_WHERE_ACTIVE_AND_STATUS_OR_MODERATORID_AND_STATUS +
            GROUP_BY_P_ID +
            ORDER_BY_TIME)
    List<PostDto> findModerationPostsData(Pageable pageRequest,
                                           long authUserId,
                                           @Param("moderationStatus") ModerationStatus moderationStatus,
                                           @Param("moderationStatusStr") String moderationStatusStr);

    @Query(value = SELECT_COUNT_POSTS +
                    WHERE_ACTIVE_AND_STATUS_OR_MODERATORID_AND_STATUS,
                    nativeQuery = true)
    Optional<Long> getTotalModerationPostsCount(@Param("authUserId") long authUserId,
                                               @Param("moderationStatusStr") String moderationStatusStr);


    @Query(JPQL_SELECT_STATISTICS_DATA +
           JPQL_WHERE_GENERAL +
           " AND u.id = :authUserId")
    StatPostsDto getMyPostsStatistics(@Param("authUserId") long authUserId);


    @Query(JPQL_SELECT_STATISTICS_DATA +
           JPQL_WHERE_GENERAL)
    StatPostsDto getAllPostsStatistics();

}