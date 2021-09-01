package integration;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
public class ApiPostControllerTest {

    @Autowired
    private MockMvc mvc;

    private final String POST1_TITLE = "Пост1";
    private final String POST2_TITLE = "Пост2";
    private final String POST3_TITLE = "Пост3";
    private final String POST4_TITLE = "Пост4";

    @Test
    public void givenNegativeOffset_whenGetPosts_thenBadRequest() throws Exception {
        mvc.perform(get("/api/post/?offset=-1"))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void givenNegativeLimit_whenGetPosts_thenBadRequest() throws Exception {
        mvc.perform(get("/api/post/?limit=-1"))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void givenIllegalMode_whenGetPosts_thenBadRequest() throws Exception {
        mvc.perform(get("/api/post/?mode=incorrect"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenGetPostsWithDefaultParams_thenOk() throws Exception {
        final int RESULT_POSTS_COUNT = 3;
        mvc.perform(get("/api/post"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(RESULT_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(RESULT_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.not(POST1_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.not(POST1_TITLE)))
                .andExpect(jsonPath("$.posts[2].title", Matchers.not(POST1_TITLE)));
    }

    @Test
    public void whenGetRecentPosts_thenOk() throws Exception {
        final int RECENT_POSTS_COUNT = 3;
        mvc.perform(get("/api/post?offset=0&limit=20&mode=recent"))
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
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", Matchers.is(BEST_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts", Matchers.hasSize(BEST_POSTS_COUNT)))
                .andExpect(jsonPath("$.posts[0].title", Matchers.is(POST4_TITLE)))
                .andExpect(jsonPath("$.posts[1].title", Matchers.is(POST3_TITLE)));

    }
}
