package diplom.blogengine.repository;

import diplom.blogengine.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface CaptchaCodeRepository extends JpaRepository<CaptchaCode, Long> {

    @Query(value = "SELECT c.code " +
            "FROM captcha_codes c " +
            "WHERE c.id = :id " +
            "LIMIT 1", nativeQuery = true)
    String findCodeById(@Param("id") long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE " +
            "FROM captcha_codes c " +
            "WHERE c.time <= :time ", nativeQuery = true)
    int deleteExpiryCaptchas(LocalDateTime time);

    @Query(value = "SELECT c.code " +
            "FROM captcha_codes c " +
            "WHERE c.secret_code = :secretCode " +
            "LIMIT 1", nativeQuery = true)
    String findCodeBySecret(@Param("secretCode") String secretCode);

    @Query(value = "SELECT c.code " +
            "FROM captcha_codes c " +
            "WHERE c.secret_code = :secretCode AND c.time > :expiredTime " +
            "LIMIT 1", nativeQuery = true)
    String findCodeBySecretAndNotExpired(@Param("secretCode") String secretCode, @Param("expiredTime") LocalDateTime expiredTime);
}
