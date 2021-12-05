package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.exception.PostNotFoundException;
import diplom.blogengine.model.Post;
import diplom.blogengine.model.dto.PostDto;
import diplom.blogengine.model.dto.PostDtoExt;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.service.IPostService;
import diplom.blogengine.service.schedule.ScheduledTasksHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import util.TestDataGenerator;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

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

    @Autowired
    private ScheduledTasksHandler scheduler;

    @BeforeEach
    public void setUp() {
        scheduler.shutdown();
    }

    @Test
    public void givenPostService_whenStart_thenNotNull() {
        assertNotNull(postService);
        assertNotNull(postRepository);
    }

    @Test
    public void givenMockPostDtoExt_whenGetSinglePost_thenCorrectResponseReturned() {
        PostDtoExt testPost = testDataGenerator.generatePostDtoExt();

        long testPostId = 1;
        long authUserId = 0;
        boolean isModerator = false;
        Mockito.when(postRepository.findPostById(testPostId, authUserId, isModerator)).thenReturn(testPost);

        SinglePostResponse response = postService.getPostDataById(testPostId, null);

        assertNotNull(response);
        assertEquals(testPost.getId(), response.getId());
        assertEquals(testPost.getTitle(), response.getTitle());
        assertEquals(response.getLikeCount(), response.getLikeCount());
        assertEquals(response.getDislikeCount(), response.getDislikeCount());
        assertEquals(response.getViewCount(), response.getViewCount());
        assertEquals(response.getText(), response.getText());
    }


    @Test
    public void givenMockNullPostData_whenGetSinglePost_thenExceptionPostNotFoundRaised() {
        long testPostId = 2;
        long authUserId = 0;
        boolean isModerator = false;
        Mockito.when(postRepository.findPostById(testPostId, authUserId, isModerator)).thenReturn(null);

        Exception exception = assertThrows(PostNotFoundException.class, () -> postService.getPostDataById(testPostId, null));

        assertNotNull(exception);

        String expectedMsg = "not found";
        String actualMsg = exception.getMessage();

        assertNotNull(actualMsg);
        assertTrue(actualMsg.contains(expectedMsg));

    }

}
