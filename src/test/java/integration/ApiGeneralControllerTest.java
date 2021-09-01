package integration;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.config.BlogSettings;
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
        "classpath:testdbsql/V1_2__insert_global_settings.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:testdbsql/delete_tables.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ApiGeneralControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BlogSettings blogSettings;


    @Test
    public void whenGetInitOptions_thenOk() throws Exception {
        mvc.perform(get("/api/init"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", Matchers.is(blogSettings.getTitle())))
                .andExpect(jsonPath("$.subtitle", Matchers.is(blogSettings.getSubtitle())))
                .andExpect(jsonPath("$.phone", Matchers.is(blogSettings.getPhone())))
                .andExpect(jsonPath("$.email", Matchers.is(blogSettings.getEmail())))
                .andExpect(jsonPath("$.copyright", Matchers.is(blogSettings.getCopyright())))
                .andExpect(jsonPath("$.copyrightFrom", Matchers.is(blogSettings.getCopyrightFrom())));
    }

    @Test
    public void whenGetSettings_thenOk() throws Exception {
        final boolean MULTIUSER_MODE = false;
        final boolean POST_PREMODERATION = true;
        final boolean STATISTICS_IS_PUBLIC = true;
        mvc.perform(get("/api/settings"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.MULTIUSER_MODE", Matchers.is(MULTIUSER_MODE)))
                .andExpect(jsonPath("$.POST_PREMODERATION", Matchers.is(POST_PREMODERATION)))
                .andExpect(jsonPath("$.STATISTICS_IS_PUBLIC", Matchers.is(STATISTICS_IS_PUBLIC)));
    }
}
