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
public class ApiTagControllerTest {

    @Autowired
    private MockMvc mvc;

    private final String TAG1_TITLE = "Java";
    private final double TAG1_WEIGHT = 1.0;
    private final String TAG2_TITLE = "Java Persistence API";
    private final double TAG2_WEIGHT = 0.67;
    private final String TAG3_TITLE = "Spring";
    private final double TAG3_WEIGHT = 0.33;
    private final String TAG4_TITLE = "JUnit";
    private final double TAG4_WEIGHT = 0;


    @Test
    public void givenIllegalLengthQuery_whenGetTags_thenBadRequest() throws Exception {
        mvc.perform(get("/api/tag/?query=" + generateIllegalLengthQuery()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenGetAllTags_thenOk() throws Exception {
        final int RESULT_TAGS_COUNT = 4;
        mvc.perform(get("/api/tag"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags", Matchers.hasSize(RESULT_TAGS_COUNT)))
                .andExpect(jsonPath("$.tags[0].name", Matchers.is(TAG1_TITLE)))
                .andExpect(jsonPath("$.tags[0].weight", Matchers.is(TAG1_WEIGHT)))
                .andExpect(jsonPath("$.tags[1].name", Matchers.is(TAG2_TITLE)))
                .andExpect(jsonPath("$.tags[1].weight", Matchers.is(TAG2_WEIGHT)))
                .andExpect(jsonPath("$.tags[2].name", Matchers.is(TAG3_TITLE)))
                .andExpect(jsonPath("$.tags[2].weight", Matchers.is(TAG3_WEIGHT)))
                .andExpect(jsonPath("$.tags[3].name", Matchers.is(TAG4_TITLE)))
                .andExpect(jsonPath("$.tags[3].weight", Matchers.is(TAG4_WEIGHT)));
    }

    private String generateIllegalLengthQuery() {
        StringBuilder builder = new StringBuilder();
        int MAX_QUERY_LENGTH = 100;
        for (int i = 0; i < MAX_QUERY_LENGTH + 1; i++ ) {
            builder.append("q");
        }
        return builder.toString();
    }

}
