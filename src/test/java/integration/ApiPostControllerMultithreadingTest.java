package integration;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import util.JsonParseHelper;
import util.RequestHelper;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = {Application.class, H2JpaConfig.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:testdbsql/V1_0__create_db_schema_tables.sql",
        "classpath:testdbsql/V1_1__add_foreign_keys.sql",
        "classpath:testdbsql/V1_2__insert_global_settings.sql",
        "classpath:testdbsql/V1_3__insert_test_data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:testdbsql/delete_tables.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("test")
public class ApiPostControllerMultithreadingTest {
    @LocalServerPort
    private int port;
    private String host;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    public void setUp() {
        host = "http://localhost:" + port;
    }

    @Autowired
    private PostRepository postRepository;

    private JsonParseHelper parseHelper = new JsonParseHelper();

    @Test
    public void givenResourceUrl_whenSendGetPostByIdParallel_thenReturnedViewCountCorrect() throws Exception {
        final long POST_ID = 3L;
        final int PARALLEL_REQUEST_COUNT = 100;
        String resourceUrl = host + "/api/post/" + POST_ID;

        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<SinglePostResponse>> listFutures = new ArrayList<>();
        for(int i = 0; i < PARALLEL_REQUEST_COUNT; i++) {
            listFutures.add(executorService.submit(() ->
                    RequestHelper.sendGetRequest(resourceUrl,
                            SinglePostResponse.class,
                            RequestHelper::assertResponseOkAndContentTypeJson,
                            testRestTemplate))
            );
        }

        for (Future f : listFutures) {
            f.get();
        }

        int expectedViewCount = PARALLEL_REQUEST_COUNT;
        int actualViewCount = postRepository.findById(POST_ID).get().getViewCount();
        System.out.println(actualViewCount);
        assertEquals(expectedViewCount, actualViewCount);
    }



}
