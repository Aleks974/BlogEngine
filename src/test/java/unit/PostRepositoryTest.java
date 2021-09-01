package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.model.ModerationStatus;
import diplom.blogengine.model.Post;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.service.util.PasswordGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

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
public class PostRepositoryTest {
    @Autowired
    PasswordGenerator passwordGenerator;

    @Autowired
    PostRepository postRepository;

    @Test
    public void givenPostWithUser_WhenSaveAndRetrievePostWithUser_thenOk() {
        // given
        Post givenPost = generatePost();
        givenPost = postRepository.save(givenPost);
        postRepository.flush();

        // when
        Post foundPost = postRepository.findById(givenPost.getId()).get();

        // then
        assertNotNull(foundPost);
        assertEquals(givenPost.getTitle(), foundPost.getTitle());
    }

    private Post generatePost(){
        Post post = new Post();
        post.setActive(true);
        post.setModerationStatus(ModerationStatus.ACCEPTED);
        post.setUser(generateUser());
        post.setTime(LocalDateTime.now());
        post.setTitle("Название поста");
        post.setText("текст поста");
        post.setViewCount(0);

        return post;
    }

    private User generateUser(){
        User user = new User();
        user.setModerator(false);
        user.setRegTime(LocalDateTime.now());
        user.setName("vasya");
        user.setEmail("test@test.ru");
        user.setPassword(passwordGenerator.generateHashEncode("password"));
        return user;
    }
}
