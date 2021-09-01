package diplom.blogengine.repository;

import diplom.blogengine.model.Post;
import diplom.blogengine.model.dto.PostDto;
import diplom.blogengine.model.dto.CommentsCountDto;
import diplom.blogengine.model.dto.VotesCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    static final String JPQL_GENERAL_WHERE_CLAUSE = "p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= NOW() ";

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE " + JPQL_GENERAL_WHERE_CLAUSE)
    Page<PostDto> findOtherPosts(Pageable pageable);


    @Query("SELECT p " +
            "FROM Post p " +
            "LEFT JOIN p.comments c " +
            "WHERE " + JPQL_GENERAL_WHERE_CLAUSE +
            "GROUP BY p.id ")
    Page<PostDto> findPopularPosts(Pageable pageable);


    @Query("SELECT p " +
            "FROM Post p " +
            "LEFT JOIN p.votes v " +
            "WHERE " + JPQL_GENERAL_WHERE_CLAUSE + " AND (v.value = 1 OR v.value IS NULL) " +
            "GROUP BY p.id ")
    Page<PostDto> findBestPosts(Pageable pageable);

    @Query(value = "SELECT COUNT(p.id) " +
            "FROM posts p " +
            "WHERE p.is_active = 1 AND p.moderation_status = 'ACCEPTED' AND p.time <= NOW() ",
           nativeQuery = true)
    long getPostsCount();

    @Query("SELECT new diplom.blogengine.model.dto.CommentsCountDto(p.id, COUNT(p.id)) " +
            "FROM Post p " +
            "JOIN p.comments c " +
            "WHERE p.id IN :postIds " +
            "GROUP BY p.id")
    List<CommentsCountDto> findCommentsCountByPostIdList(@Param("postIds") Collection<Long> postIds);


    @Query("SELECT new diplom.blogengine.model.dto.VotesCountDto(p.id, COUNT(CASE WHEN (v.value = 1) THEN 1 ELSE NULL END), COUNT(CASE WHEN (v.value = -1) THEN 1 ELSE NULL END)) " +
            "FROM Post p " +
            "JOIN p.votes v " +
            "WHERE p.id IN :postIds " +
            "GROUP BY p.id")
    List<VotesCountDto> findVotesCountByPostIdList(@Param("postIds") List<Long> postsIds);


}