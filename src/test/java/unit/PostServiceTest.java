package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.model.Post;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.service.IPostService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import util.TestDataGenerator;

import java.util.Collections;

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
public class PostServiceTest {

    @Autowired
    private IPostService postService;

    @MockBean
    PostRepository postRepository;

    private TestDataGenerator testDataGenerator = new TestDataGenerator();

    @Test
    public void givenPostService_whenStart_thenNotNull() {
        assertNotNull(postService);
        assertNotNull(postRepository);
    }

    @Test
    public void givenMockPostData_whenGetSinglePost_thenResponseReturned() {
        final long TEST_POST_ID = 1;
        final long TEST_LIKE_COUNT = 23;
        final long TEST_DISLIKE_COUNT = 4;
        Post testPost = testDataGenerator.generatePost();
        Object[] mockPostData = {testPost, null, TEST_LIKE_COUNT, TEST_DISLIKE_COUNT};

        Mockito.when(postRepository.findPostById(TEST_POST_ID)).thenReturn(Collections.singletonList(mockPostData));
        SinglePostResponse response = postService.getPostDataById(TEST_POST_ID);

        assertNotNull(response);
        assertEquals(testPost.getId(), response.getId());
        assertEquals(testPost.getTitle(), response.getTitle());
        assertEquals(response.getLikeCount(), TEST_LIKE_COUNT);
        assertEquals(response.getDislikeCount(), TEST_DISLIKE_COUNT);
    }

}