package diplom.blogengine.repository;

import diplom.blogengine.model.PostVote;
import diplom.blogengine.model.dto.StatVotesDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    static final String JPQL_SELECT_STATISTICS_DATA = "SELECT new diplom.blogengine.model.dto.StatVotesDto(COUNT(DISTINCT CASE WHEN (v.value = 1) THEN v.id ELSE NULL END), " +
                                                        "COUNT(DISTINCT CASE WHEN (v.value = -1) THEN v.id ELSE NULL END)) " +
                                                        "FROM PostVote v " +
                                                        "JOIN v.user u " +
                                                        "JOIN v.post p ";
    static final String JPQL_WHERE_GENERAL = "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= NOW() ";

            @Query(value = "SELECT * " +
            "FROM post_votes v " +
            "WHERE v.post_id = :postId AND v.user_id = :userId " +
            "LIMIT 1", nativeQuery = true)
    PostVote findByPostAndUserIds(@Param("postId") long postId, @Param("userId") long userId);


    @Query( JPQL_SELECT_STATISTICS_DATA +
            JPQL_WHERE_GENERAL +
            "AND u.id = :authUserId")
    StatVotesDto getMyVotesStatistics(@Param("authUserId") long authUserId);

    @Query(JPQL_SELECT_STATISTICS_DATA +
            JPQL_WHERE_GENERAL)
    StatVotesDto getAllVotesStatistics();

}

