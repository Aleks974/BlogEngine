package diplom.blogengine.repository;

import diplom.blogengine.model.PostComment;
import diplom.blogengine.model.User;
import diplom.blogengine.model.dto.CommentDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query(value = "SELECT c.post_id " +
            "FROM post_comments c " +
            "WHERE c.id = :id " +
            "LIMIT 1", nativeQuery = true)
    Optional<Long> findPostIdByCommentId(long id);

    @Query(value = "SELECT new diplom.blogengine.model.dto.CommentDto(pc.id, pc.time, pc.text, u.id, u.name, u.photo) " +
            "FROM PostComment pc " +
            "JOIN pc.user u " +
            "JOIN pc.post p " +
            "WHERE p.id = :postId " +
            "ORDER BY pc.time ASC")
    List<CommentDto> findByPostId(@Param("postId") long postId);
}

