package diplom.blogengine.repository;

import diplom.blogengine.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    static final String JPQL_SELECT_COUNT_POSTS = "SELECT COUNT(distinct p.id) " +
                                                  "FROM Post p ";
    static final String JPQL_SELECT_POSTS_DATA = "SELECT p, u, COUNT(DISTINCT CASE WHEN (v.value = 1) THEN v.id ELSE NULL END) AS like_count, COUNT(DISTINCT CASE WHEN (v.value = -1) THEN v.id ELSE NULL END) AS dislike_count,  COUNT(DISTINCT c.id) AS comment_count " +
                                                 "FROM Post p " +
                                                 "JOIN p.user u " +
                                                 "LEFT JOIN p.votes v " +
                                                 "LEFT JOIN p.comments c ";

    static final String JPQL_JOIN_P_TAGS = " JOIN p.tags t ";
    static final String JPQL_JOIN_P_VOTES = " LEFT JOIN p.votes v ";

    static final String JPQL_WHERE_GENERAL_CLAUSE = "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= NOW() ";
    static final String WHERE_SEARCH_BY_PARAM_QUERY_CLAUSE = " AND (p.title LIKE %:query% OR p.text LIKE %:query%) ";
    static final String WHERE_SEARCH_BY_PARAM_DATE_CLAUSE = " AND DATE(p.time) = :date ";
    static final String WHERE_SEARCH_BY_PARAM_TAG_CLAUSE = " AND t.name = :tag ";
    static final String WHERE_VOTES_EXCLUDE_DISLIKE_CLAUSE = " AND (v.value = NULL OR v.value = 1) ";

    static final String GROUP_BY_P_ID = " GROUP BY p.id ";

    static final String HAVING_VOTES_LIKE_CLAUSE = " HAVING col_2_0_ > 0 OR (col_2_0_ = 0 AND col_3_0_ = 0) ";

    static final String ORDER_BY_LIKE_COUNT = " ORDER BY like_count DESC ";
    static final String ORDER_BY_COMMENT_COUNT = " ORDER BY comment_count DESC ";


    @Query(JPQL_SELECT_POSTS_DATA +
            JPQL_WHERE_GENERAL_CLAUSE +
           GROUP_BY_P_ID)
    List<Object[]> findPostsData(Pageable pageRequestWithSort);

    @Query(JPQL_SELECT_COUNT_POSTS +
           JPQL_WHERE_GENERAL_CLAUSE)
    long getTotalPostsCount();


    @Query(JPQL_SELECT_POSTS_DATA +
           JPQL_WHERE_GENERAL_CLAUSE +
           GROUP_BY_P_ID +
           HAVING_VOTES_LIKE_CLAUSE +
           ORDER_BY_LIKE_COUNT)
    List<Object[]> findPostsDataOrderByLikesCount(Pageable pageRequest);

    @Query(JPQL_SELECT_COUNT_POSTS +
           JPQL_JOIN_P_VOTES +
           JPQL_WHERE_GENERAL_CLAUSE +
           WHERE_VOTES_EXCLUDE_DISLIKE_CLAUSE)
    long getTotalPostsCountExcludeDislikes();


    @Query( JPQL_SELECT_POSTS_DATA +
            JPQL_WHERE_GENERAL_CLAUSE +
            GROUP_BY_P_ID +
            ORDER_BY_COMMENT_COUNT)
    List<Object[]> findPostsDataOrderByCommentsCount(Pageable pageRequest);


    @Query(JPQL_SELECT_POSTS_DATA +
           JPQL_WHERE_GENERAL_CLAUSE + WHERE_SEARCH_BY_PARAM_QUERY_CLAUSE +
           GROUP_BY_P_ID)
    List<Object[]> findPostsByQuery(@Param("query") String query, Pageable pageRequestWithSort);

    @Query(JPQL_SELECT_COUNT_POSTS +
            JPQL_WHERE_GENERAL_CLAUSE + WHERE_SEARCH_BY_PARAM_QUERY_CLAUSE)
    long getTotalPostsCountByQuery(@Param("query") String query);


    @Query(JPQL_SELECT_POSTS_DATA +
           JPQL_WHERE_GENERAL_CLAUSE + WHERE_SEARCH_BY_PARAM_DATE_CLAUSE +
           GROUP_BY_P_ID)
    List<Object[]> findPostsByDate(@Param("date") Date date, Pageable pageRequestWithSort);

    @Query(JPQL_SELECT_COUNT_POSTS +
            JPQL_WHERE_GENERAL_CLAUSE + WHERE_SEARCH_BY_PARAM_DATE_CLAUSE)
    long getTotalPostsCountByDate(@Param("date") Date date);


    @Query(JPQL_SELECT_POSTS_DATA +
           JPQL_JOIN_P_TAGS +
           JPQL_WHERE_GENERAL_CLAUSE + WHERE_SEARCH_BY_PARAM_TAG_CLAUSE +
           GROUP_BY_P_ID)
    List<Object[]> findPostsByTag(@Param("tag") String tag, Pageable pageRequestWithSort);

    @Query(JPQL_SELECT_COUNT_POSTS +
           JPQL_JOIN_P_TAGS +
           JPQL_WHERE_GENERAL_CLAUSE + WHERE_SEARCH_BY_PARAM_TAG_CLAUSE)
    long getTotalPostsCountByTag(@Param("tag") String tag);


    @Query("SELECT YEAR(p.time) AS posts_year " +
            "FROM Post p " +
            JPQL_WHERE_GENERAL_CLAUSE +
            "GROUP BY posts_year " +
            "ORDER BY posts_year ASC")
    List<Integer> findYears();

    @Query("SELECT DATE(p.time) AS posts_date, COUNT(p.id) " +
            "FROM Post p " +
            JPQL_WHERE_GENERAL_CLAUSE + " AND YEAR(p.time) = :year " +
            "GROUP BY posts_date " +
            "ORDER BY posts_date DESC")
    List<Object[]> findPostsCountPerDateByYear(@Param("year" ) Integer year);

    @Query("SELECT p, u, COUNT(CASE WHEN (v.value = 1) THEN 1 ELSE NULL END) AS like_count, COUNT(CASE WHEN (v.value = -1) THEN 1 ELSE NULL END) AS dislike_count " +
            "FROM Post p " +
            "JOIN p.user u " +
            "LEFT JOIN p.votes v " +
            JPQL_WHERE_GENERAL_CLAUSE + " AND p.id = :id " +
            "GROUP BY p.id")
    List<Object[]> findPostById(@Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE posts p SET p.view_count = p.view_count + 1 WHERE p.id = :id LIMIT 1"
                , nativeQuery = true)
    int updatePostViewCount(@Param("id") long id);

    //////////// TEST 1
    @Query("SELECT p " +
            "FROM Post p " +
            "LEFT JOIN User u ON u.id = p.user " +
            "LEFT JOIN PostComment pc ON pc.post = p.id " +
            "LEFT JOIN PostVote pvl on pvl.post = p.id and pvl.value = 1 " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_DATE() " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(pvl) DESC"
    )
    Page<Post> findPostsOrderByLikes(Pageable pageable);

    //////////// TEST 2

    @Query("SELECT p, pc, pvl " +
            "FROM Post p " +
            "LEFT JOIN p.user u " +
            "LEFT JOIN p.comments pc " +
            "LEFT JOIN p.votes pvl " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_DATE() AND pvl.value = 1 " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(pvl) DESC"
    )
    Page<Post> findPostsOrderByLikes2(Pageable pageable);



}