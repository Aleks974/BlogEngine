package diplom.blogengine.repository;

import diplom.blogengine.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface PasswordTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    @Modifying
    @Transactional
    @Query(value = "DELETE " +
            "FROM password_reset_tokens t " +
            "WHERE t.expiry_date <= :time ", nativeQuery = true)
    int deleteExpiryTokens(@Param("time") LocalDateTime time);
}
