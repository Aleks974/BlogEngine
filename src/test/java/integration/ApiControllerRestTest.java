package integration;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.api.request.UserLoginRequest;
import diplom.blogengine.api.request.UserRegisterDataRequest;
import diplom.blogengine.api.response.AuthResponse;
import diplom.blogengine.api.response.MultiplePostsResponse;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.SinglePostResponse;
import diplom.blogengine.config.BlogSettings;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.util.UriComponentsBuilder;
import util.TestDataGenerator;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

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
public class ApiControllerRestTest {
    @LocalServerPort
    protected int port;

    protected String host;

    @PersistenceContext
    protected EntityManager entityManager;

    protected final TestRestTemplate testRestTemplate = new TestRestTemplate();
    /* @Autowired
    private TestRestTemplate testRestTemplate;*/
    protected final TestDataGenerator testDataGenerator = new TestDataGenerator();

    protected final long moderatorId = 1;
    protected final String moderatorEmail = "a@aa.ru";
    protected final String moderatorPass = "password1";
    protected final long user3Id = 3;
    protected final String user3Email = "c@cc.ru";
    protected final String user3Pass = "password3";

    @BeforeEach
    public void beforeEach() {
        host = "http://localhost:" + port;
    }

    public String getCookieAfterSuccessLogin(String userEmail, String userPass) {
        ResponseEntity<AuthResponse> responseEntity = loginUserAndAssert(userEmail, userPass);
        String cookie = getCookieFromResponse(responseEntity);
        assertNotNull(cookie);
        return cookie;
    }

    public ResponseEntity<AuthResponse> loginUserAndAssert(String userEmail, String userPass) {
        ResponseEntity<AuthResponse> responseEntity = loginUser(userEmail, userPass);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        AuthResponse authResponse = responseEntity.getBody();
        assertNotNull(authResponse);
        assertTrue(authResponse.isResult());
        assertNotNull(authResponse.getUser());
        assertEquals(userEmail, authResponse.getUser().getEmail());

        return responseEntity;
    }

    public ResponseEntity<AuthResponse> loginUserFailAndAssert(String userEmail, String userPass) {
        ResponseEntity<AuthResponse> responseEntity = loginUser(userEmail, userPass);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        AuthResponse authResponse = responseEntity.getBody();
        assertNotNull(authResponse);
        assertFalse(authResponse.isResult());
        assertNull(authResponse.getUser());
        return responseEntity;
    }

    public ResponseEntity<AuthResponse> loginUser(String userEmail, String userPass) {
        String authUrl = "/api/auth/login";
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        URI authUri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(authUrl)
                .build()
                .toUri();
        UserLoginRequest loginRequest = new UserLoginRequest(userEmail, userPass);
        return testRestTemplateLocal.postForEntity(authUri, new HttpEntity<>(loginRequest), AuthResponse.class);
    }

    public String getCookieFromResponse(ResponseEntity<?> responseEntity) {
        String cookie = null;
        List<String> headersList;
        if (responseEntity != null && (headersList = responseEntity.getHeaders().get("Set-Cookie")) != null) {
            cookie = headersList.get(0);
        }
        return cookie;
    }

    protected void assertResultResponseBadRequest(ResponseEntity<ResultResponse> responseEntity) {
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ResultResponse response = responseEntity.getBody();
        assertFalse(response.getResult());
        assertNotNull(response.getErrors());
    }

    public ResponseEntity<SinglePostResponse> sendGetPost(long postId, String...cookie) {
        String resourceUrl = "/api/post/" + postId;
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        HttpHeaders httpHeaders = new HttpHeaders();
        if (cookie != null && cookie.length == 1) {
            httpHeaders.set("Cookie", cookie[0]);
        }
        HttpEntity<UserRegisterDataRequest> entity = new HttpEntity<>(httpHeaders);
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        return testRestTemplateLocal.exchange(uri, HttpMethod.GET, entity, SinglePostResponse.class);
    }

    protected void clearTmpUploadDir() throws IOException {
        Path tmpDir = Path.of("upload_tmp");
        if (Files.exists(tmpDir)) {
            Files.walkFileTree(tmpDir, new FileVisitorImpl());
        }
    }

    protected void clearTmpFile(Path path) throws IOException {
        Files.delete(path);
    }

    protected static class FileVisitorImpl extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            } else {
                // directory iteration failed
                throw exc;
            }
        }
    }

    @FunctionalInterface
    public interface SendHTTPRequest<T, V> {
        V send(T request, String Cookie);
    }
}
