package integration;


import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.model.dto.PostDtoExt;
import diplom.blogengine.repository.CachedPostRepository;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.repository.PostsCounterStorage;
import diplom.blogengine.service.schedule.ScheduledTasksHandler;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import util.TestDataGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@SpringBootTest(classes = {Application.class, H2JpaConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Sql(scripts = {"classpath:testdbsql/V1_0__create_db_schema_tables.sql",
        "classpath:testdbsql/V1_1__add_foreign_keys.sql",
        "classpath:testdbsql/V1_2__insert_global_settings.sql",
        "classpath:testdbsql/V1_3__insert_test_data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:testdbsql/delete_tables.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("test")
public class CachedPostRepositoryTest {

    @Autowired
    private CachedPostRepository cachedPostRepository;

    @Autowired
    PostsCounterStorage counterStorage;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PostRepository postRepository;

    @Autowired
    private ScheduledTasksHandler scheduler;

    @BeforeEach
    public void setUp() {
        scheduler.shutdown();
    }

    private final TestDataGenerator testDataGenerator = new TestDataGenerator();

    private final String POST2_TITLE = "Пост2";


    @Test
    public void givenMockedPostDtoExt_whenGetSinglePost_thenDataCachedAndNextRequestGetDataFromCache() throws Exception {
        final int testPostId = 1;
        final int authUserId = 0;
        final boolean isModerator = false;
        PostDtoExt postDtoExt = testDataGenerator.generatePostDtoExt();

        Mockito.when(postRepository.findPostById(testPostId, authUserId, isModerator))
                .thenReturn(postDtoExt);

        String resourceUrl = "/api/post/" + testPostId;
        mvc.perform(get(resourceUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", Matchers.is(testPostId)))
                .andExpect(jsonPath("$.title", Matchers.is(postDtoExt.getTitle())));

        Mockito.when(postRepository.findPostById(testPostId, authUserId, isModerator))
                .thenReturn(null);

        // from cache
        mvc.perform(get(resourceUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", Matchers.is(testPostId)))
                .andExpect(jsonPath("$.title", Matchers.is(postDtoExt.getTitle())))
                .andExpect(jsonPath("$.likeCount", Matchers.is((int)postDtoExt.getLikeCount())))
                .andExpect(jsonPath("$.dislikeCount", Matchers.is((int)postDtoExt.getDislikeCount())))
                .andExpect(jsonPath("$.viewCount", Matchers.is((int)postDtoExt.getViewCount())));
    }
}
