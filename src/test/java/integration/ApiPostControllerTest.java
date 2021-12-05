package integration;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.service.MyPostStatus;
import diplom.blogengine.service.schedule.ScheduledTasksHandler;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

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
public class ApiPostControllerTest {

    @Autowired
    private MockMvc mvc;

    private final String POST1_TITLE = "Пост1";
    private final String POST2_TITLE = "Пост2";
    private final String POST3_TITLE = "Пост3";
    private final String POST4_TITLE = "Пост4";

    @Autowired
    private ScheduledTasksHandler scheduler;

    @BeforeEach
    public void setUp() {
        scheduler.shutdown();
    }

    @Test
    public void givenNegativeOffset_whenGetPosts_thenBadRequest() throws Exception {
        mvc.perform(get("/api/post/?offset=-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNegativeLimit_whenGetPosts_thenBadRequest() throws Exception {
        mvc.perform(get("/api/post/?limit=-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenIllegalMode_whenGetPosts_thenBadRequest() throws Exception {
        mvc.perform(get("/api/post/?mode=incorrect"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenGetPostsWithDefaultParams_thenOk() throws Exception {
        final int RESULT_POSTS_COUNT = 3;
        MvcResult result = mvc.perform(get("/api/post"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(RESULT_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(RESULT_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.not(POST1_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.not(POST1_TITLE)))
                .andExpect(jsonPath("$.posts[2].title", Matchers.not(POST1_TITLE)))
                .andReturn();
    }

    @Test
    public void whenGetRecentPosts_thenOk() throws Exception {
        final int RECENT_POSTS_COUNT = 3;
        mvc.perform(get("/api/post?offset=0&limit=20&mode=recent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(RECENT_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(RECENT_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.is(POST4_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.is(POST3_TITLE)))
                .andExpect(jsonPath("$.posts[2].title", Matchers.is(POST2_TITLE)));

    }

    @Test
    public void whenGetEarlyPosts_thenOk() throws Exception {
        final int EARLY_POSTS_COUNT = 3;
        mvc.perform(get("/api/post?offset=0&limit=20&mode=early"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(EARLY_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(EARLY_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.is(POST2_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.is(POST3_TITLE)))
                .andExpect(jsonPath("$.posts[2].title", Matchers.is(POST4_TITLE)));

    }

    @Test
    public void whenGetPopularPosts_thenOk() throws Exception {
        final int POPULAR_POSTS_COUNT = 3;
        mvc.perform(get("/api/post?offset=0&limit=20&mode=popular"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(POPULAR_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(POPULAR_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.is(POST2_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.is(POST3_TITLE)))
                .andExpect(jsonPath("$.posts[2].title", Matchers.is(POST4_TITLE)));

    }

    @Test
    public void whenGetBestPosts_thenOk() throws Exception {
        final int BEST_POSTS_COUNT = 2;
        mvc.perform(get("/api/post?offset=0&limit=20&mode=best"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(BEST_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(BEST_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.is(POST4_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.is(POST3_TITLE)));

    }


    @Test
    public void whenGetPostsByQuery_thenOk() throws Exception {
        final int QUERY_POSTS_COUNT = 2;
        final String QUERY = "для";
        mvc.perform(get("/api/post/search?offset=0&limit=20&query=" + QUERY))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(QUERY_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(QUERY_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.is(POST3_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.is(POST2_TITLE)));

    }

    @Test
    public void whenGetPostsByDate_thenOk() throws Exception {
        final int QUERY_POSTS_COUNT = 2;
        final String DATE_QUERY = "2021-08-12";
        mvc.perform(get("/api/post/byDate?offset=0&limit=20&date=" + DATE_QUERY))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(QUERY_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(QUERY_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.is(POST4_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.is(POST3_TITLE)));

    }

    @Test
    public void whenGetPostsByWrongDate_then400BadRequest() throws Exception {
        final String WRONG_DATE_QUERY = "202-08-11";
        mvc.perform(get("/api/post/byDate?offset=0&limit=20&date=" + WRONG_DATE_QUERY))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void whenGetPostsByTag_thenOk() throws Exception {
        final int QUERY_POSTS_COUNT = 3;
        final String TAG_QUERY = "Java";
        mvc.perform(get("/api/post/byTag?offset=0&limit=20&tag=" + TAG_QUERY))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(QUERY_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(QUERY_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.is(POST4_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.is(POST3_TITLE)))
                .andExpect(jsonPath("$.posts[2].title", Matchers.is(POST2_TITLE)));

    }

    @Test
    public void givenId_whenGetSinglePost_thenOk() throws Exception {
        final int POST_ID = 2;
        final int LIKE_COUNT = 0;
        final int DISLIKE_COUNT = 1;
        final int VIEW_COUNT = 2;
        mvc.perform(get("/api/post/" + POST_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", Matchers.is(POST_ID)))
                .andExpect(jsonPath("$.title", Matchers.is(POST2_TITLE)))
                .andExpect(jsonPath("$.likeCount", Matchers.is(LIKE_COUNT)))
                .andExpect(jsonPath("$.dislikeCount", Matchers.is(DISLIKE_COUNT)))
                .andExpect(jsonPath("$.viewCount", Matchers.is(VIEW_COUNT)));
    }

    @Test
    public void givenNotExistedId_whenGetSinglePost_then404() throws Exception {
        final int NOTEXISTED_POST_ID = 20;;
        mvc.perform(get("/api/post/" + NOTEXISTED_POST_ID))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    public void givenYear_whenGetCalendarData_ThenOk() throws Exception {
        final int YEAR = 2021;
        final int YEAR_COUNT = 1;
        final String ENTRY_KEY1 = "2021-08-11";
        final int ENTRY_VALUE1 = 1;
        final String ENTRY_KEY2 = "2021-08-12";
        final int ENTRY_VALUE2 = 2;
        MvcResult mvcResult = mvc.perform(get("/api/calendar?year=" + YEAR))
                .andDo(print())
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.years", Matchers.hasSize(YEAR_COUNT)))
                .andExpect(jsonPath("$.years[0]", Matchers.is(YEAR)))
                .andExpect(jsonPath("$.posts", Matchers.hasEntry(ENTRY_KEY2, ENTRY_VALUE2)))
                .andExpect(jsonPath("$.posts", Matchers.hasEntry(ENTRY_KEY1, ENTRY_VALUE1)))
                .andReturn();
        //log.debug(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void givenWrongYear_whenGetCalendarData_Then400BadRequest() throws Exception {
        final String YEAR = "1000f";
        mvc.perform(get("/api/calendar?year=" + YEAR))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
