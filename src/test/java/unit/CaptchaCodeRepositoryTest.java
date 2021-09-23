package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.model.CaptchaCode;
import diplom.blogengine.repository.CaptchaCodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {Application.class, H2JpaConfig.class},
                webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Sql(scripts = {"classpath:testdbsql/V1_0__create_db_schema_tables.sql",
        "classpath:testdbsql/V1_1__add_foreign_keys.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:testdbsql/delete_tables.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@EnableTransactionManagement
@ActiveProfiles("test")
public class CaptchaCodeRepositoryTest {

    @Autowired
    private CaptchaCodeRepository captchaCodeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void givenCaptchaCode_WhenSaveCaptchaCode_thenOk() {
        // given
        CaptchaCode captchaCode = generateCaptchaCode();
        captchaCode = captchaCodeRepository.save(captchaCode);
        captchaCodeRepository.flush();

        // when
        CaptchaCode foundCaptchaCode = entityManager.find(CaptchaCode.class, captchaCode.getId());

        // then
        assertNotNull(foundCaptchaCode);
        assertEquals(captchaCode.getCode(), foundCaptchaCode.getCode());
    }


    @Test
    public void givenCaptchaCode_WhenRetrieveCaptchaCode_thenOk() {
        // given
        CaptchaCode captchaCode = generateCaptchaCode();
        captchaCodeRepository.saveAndFlush(captchaCode);

        // when
        CaptchaCode foundCaptchaCode = captchaCodeRepository.findById(captchaCode.getId()).orElse(null);

        // then
        assertNotNull(foundCaptchaCode);
        assertEquals(captchaCode.getCode(), foundCaptchaCode.getCode());
    }

    @Transactional
    private void saveCaptchaCode(CaptchaCode captchaCode) {
        //entityManager.getTransaction().begin();
        //entityManager.persist(captchaCode);
        //entityManager.getTransaction().commit();
    }

    private CaptchaCode generateCaptchaCode(){
        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCode("123");
        captchaCode.setSecretCode("");
        captchaCode.setTime(LocalDateTime.now());

        return captchaCode;
    }

}
