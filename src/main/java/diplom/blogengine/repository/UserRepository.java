package diplom.blogengine.repository;

import diplom.blogengine.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    User findByEmail(String email);

    @Query(value = "SELECT u.id " +
            "FROM users u " +
            "WHERE u.email = :email " +
            "LIMIT 1", nativeQuery = true)
    Optional<Long> findUserIdByEmail(@Param("email") String email);


    @Query(value = "SELECT u.id " +
            "FROM users u " +
            "WHERE u.name = :name " +
            "LIMIT 1", nativeQuery = true)
    Optional<Long> findUserIdByName(@Param("name") String name);


    @Query(value = "SELECT u.name " +
            "FROM users u " +
            "WHERE u.id = :id " +
            "LIMIT 1", nativeQuery = true)
    String findNameById(@Param("id") long id);
}
