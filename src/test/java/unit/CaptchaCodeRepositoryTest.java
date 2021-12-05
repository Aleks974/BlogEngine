package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.model.CaptchaCode;
import diplom.blogengine.repository.CaptchaCodeRepository;
import diplom.blogengine.service.schedule.ScheduledTasksHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import util.TestDataGenerator;

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

    @Autowired
    private TransactionTemplate transactionTemplate;

    private TestDataGenerator testDataGenerator = new TestDataGenerator();

    @Autowired
    private ScheduledTasksHandler scheduler;

    @BeforeEach
    public void setUp() {
        scheduler.shutdown();
    }

    @Test
    public void givenCaptchaCode_WhenSaveCaptchaCode_thenOk() {
        // given
        CaptchaCode captchaCode = testDataGenerator.generateCaptchaCode();
        captchaCode = captchaCodeRepository.saveAndFlush(captchaCode);

        // when
        CaptchaCode foundCaptchaCode = entityManager.find(CaptchaCode.class, captchaCode.getId());

        // then
        assertNotNull(foundCaptchaCode);
        assertEquals(captchaCode.getCode(), foundCaptchaCode.getCode());
    }


    @Test
    public void givenCaptchaCode_WhenRetrieveCode_thenOk() {
        // given
        CaptchaCode captchaCode = testDataGenerator.generateCaptchaCode();
        transactionTemplate.execute(tx -> {
            entityManager.persist(captchaCode);
            tx.flush();
            return null;
        });
        entityManager.detach(captchaCode);

        // when
        String code  = captchaCodeRepository.findCodeBySecret(captchaCode.getSecretCode());

        // then
        assertNotNull(code);
        assertEquals(captchaCode.getCode(), code);
    }

}
