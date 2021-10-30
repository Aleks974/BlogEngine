package diplom.blogengine.repository;

import diplom.blogengine.model.PostComment;
import diplom.blogengine.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query(value = "SELECT c.post_id " +
            "FROM post_comments c " +
            "WHERE c.id = :id " +
            "LIMIT 1", nativeQuery = true)
    Optional<Long> findPostIdByCommentId(long id);
}

