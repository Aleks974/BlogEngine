package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.service.util.PasswordHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import util.TestDataGenerator;

import java.security.NoSuchAlgorithmException;
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
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHelper passwordHelper;

    private TestDataGenerator testDataGenerator = new TestDataGenerator();

    @Test
    public void givenUser_WhenSaveAndFindById_thenOk() {
        // given
        User user = testDataGenerator.generateUser();
        user = userRepository.saveAndFlush(user);

        // when
        User foundUser = userRepository.findById(user.getId()).get();

        // then
        assertNotNull(foundUser);
        assertEquals(user.getName(), foundUser.getName());
    }

    @Test
    public void givenUser_WhenSaveAndFindByEmail_thenOk() {
        // given
        User user = testDataGenerator.generateUser();
        user = userRepository.save(user);
        userRepository.flush();

        // when
        Long actualUserId = userRepository.findUserIdByEmail(user.getEmail());

        // then
        assertNotNull(actualUserId);
        assertEquals(user.getId(), actualUserId);
    }

    @Test
    public void givenUser_WhenSaveAndFindByName_thenOk() {
        // given
        User user = testDataGenerator.generateUser();
        user = userRepository.save(user);
        userRepository.flush();

        // when
        Long actualUserId = userRepository.findUserIdByName(user.getName());

        // then
        assertNotNull(actualUserId);
        assertEquals(user.getId(), actualUserId);
    }



}
