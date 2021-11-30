package diplom.blogengine.repository;

import diplom.blogengine.model.Tag;
import diplom.blogengine.model.dto.TagCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    static final String JPQL_GENERAL_WHERE_CLAUSE = "p.id = NULL OR (p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= NOW()) ";

    @Query("SELECT new diplom.blogengine.model.dto.TagCountDto(t.name, COUNT(p.id) AS tag_count) " +
            "FROM Tag t " +
            "LEFT JOIN t.posts p " +
            "WHERE " + JPQL_GENERAL_WHERE_CLAUSE +
            "GROUP BY t.id " +
            "ORDER BY tag_count DESC ")
    List<TagCountDto> findAllTagsCounts();


    @Query("SELECT new diplom.blogengine.model.dto.TagCountDto(t.name, COUNT(p.id) AS tag_count) " +
            "FROM Tag t " +
            "LEFT JOIN t.posts p " +
            "WHERE (" + JPQL_GENERAL_WHERE_CLAUSE + ") AND t.name LIKE :query% " +
            "GROUP BY t.id " +
            "ORDER BY tag_count DESC")
    List<TagCountDto> findTagsCountsByQuery(@Param("query") String query);

    @Query(value = "SELECT COUNT(p.id) AS tag_count " +
            "FROM tags t " +
            "LEFT JOIN tag2post tp ON t.id = tp.tag_id " +
            "LEFT JOIN posts p ON tp.post_id = p.id " +
            "WHERE p.is_active = 1 AND p.moderation_status = 'ACCEPTED' AND p.time <= NOW() " +
            "GROUP BY t.id " +
            "ORDER BY tag_count DESC " +
            "LIMIT 1", nativeQuery = true)
    long findMaxTagCount();


    Tag findByName(String name);
}