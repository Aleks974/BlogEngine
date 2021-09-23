package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.model.ModerationStatus;
import diplom.blogengine.model.Post;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.service.util.PasswordHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import util.TestDataGenerator;

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
public class PostRepositoryTest {
    @Autowired
    private PasswordHelper passwordHelper;

    @Autowired
    private PostRepository postRepository;

    private TestDataGenerator testDataGenerator = new TestDataGenerator();

    @Test
    public void givenPostWithUser_WhenSaveAndRetrievePostWithUser_thenOk() {
        // given
        Post givenPost = testDataGenerator.generatePost();
        givenPost = postRepository.saveAndFlush(givenPost);

        // when
        Post foundPost = postRepository.findById(givenPost.getId()).get();

        // then
        assertNotNull(foundPost);
        assertEquals(givenPost.getTitle(), foundPost.getTitle());
    }

}
