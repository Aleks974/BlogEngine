package diplom.blogengine.repository;

import diplom.blogengine.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    @Query(value = "SELECT * FROM password_reset_tokens WHERE user_id = :userId", nativeQuery = true)
    PasswordResetToken findByUserId(@Param("userId") long userId);
}
