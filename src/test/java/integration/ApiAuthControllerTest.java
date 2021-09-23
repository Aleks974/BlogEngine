package integration;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.api.request.UserDataRequest;
import diplom.blogengine.api.response.AuthCheckResponse;
import diplom.blogengine.api.response.CaptchaResponse;
import diplom.blogengine.api.response.RegisterUserResponse;
import diplom.blogengine.model.CaptchaCode;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.CaptchaCodeRepository;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.service.CaptchaService;
import diplom.blogengine.service.ICaptchaService;
import diplom.blogengine.service.util.PasswordHelper;
import jdk.jfr.ContentType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.Assert;
import util.AssertFunctional;
import util.JsonParseHelper;
import util.RequestHelper;
import util.TestDataGenerator;


import javax.persistence.EntityManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class ApiAuthControllerTest {
    @LocalServerPort
    private int port;
    private String host;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private CaptchaCodeRepository captchaCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        host = "http://localhost:" + port;
    }

    @Autowired
    private PasswordHelper passwordHelper;

    private TestDataGenerator testDataGenerator = new TestDataGenerator();

    @Test
    public void givenResourceUrl_whenSendGetAuthCheck_thenStatusOkAndResultFieldEquals() throws Exception {
        String resourceUrl = host + "/api/auth/check";
        AuthCheckResponse authCheckResponse = RequestHelper.getRequest(resourceUrl,
                                                        AuthCheckResponse.class,
                                                        RequestHelper::assertResponseOkAndContentTypeJson,
                                                        testRestTemplate);

        assertNotNull(authCheckResponse);
        boolean expectedResultField = false;
        boolean actualResultField = authCheckResponse.isResult();
        assertEquals(expectedResultField, actualResultField);
    }


    @Test
    public void givenResourceUrl_whenSendGetCaptcha_thenCaptchaSavedAndReturnedCorrect() throws Exception {
        String resourceUrl = host + "/api/auth/captcha";
        CaptchaResponse captchaResponse = RequestHelper.getRequest(resourceUrl,
                                                    CaptchaResponse.class,
                                                    RequestHelper::assertResponseOkAndContentTypeJson,
                                                    testRestTemplate);

        assertNotNull(captchaResponse);
        String IMAGE_CONTENT_TYPE = "data:image/png;base64";
        assertTrue(captchaResponse.getImage().startsWith(IMAGE_CONTENT_TYPE));
        long captchaId = Long.parseLong(captchaResponse.getSecret());
        assertNotNull(captchaCodeRepository.findCodeById(captchaId));

    }

    @Test
    public void givenResourceUrlAndCaptcha_whenSendPostUser_thenUserRegisteredAndResponseCorrect() throws Exception {
        String resourceUrl = host + "/api/auth/register";
        long usersCountBeforeReg = userRepository.count();
        CaptchaCode captchaCode = generateAndSaveCaptchaCode();

        String resourceJson = testDataGenerator.generateUserDataRequestJson(captchaCode.getCode(), String.valueOf(captchaCode.getId()));
        RegisterUserResponse registerUserResponse = RequestHelper.postRequestJson(resourceUrl,
                                                                    resourceJson,
                                                                    UserDataRequest.class,
                                                                    RegisterUserResponse.class,
                                                                    RequestHelper::assertResponseOkAndContentTypeJson,
                                                                    testRestTemplate);
        assertNotNull(registerUserResponse);
        assertThat(registerUserResponse.isResult(), equalTo(true));

        long usersCountAfterReg = userRepository.count();
        assertThat(usersCountAfterReg, equalTo(usersCountBeforeReg + 1));
    }


    @Test
    public void givenResourceUrlAndCaptcha_whenSendPostUserWithWrongCode_thenUserNotRegisteredAndResponseCorrect() throws Exception {
        String resourceUrl = host + "/api/auth/register";
        long usersCountBeforeReg = userRepository.count();
        CaptchaCode captchaCode = generateAndSaveCaptchaCode();

        String resourceJson = testDataGenerator.generateUserDataRequestJson("wrong_code", String.valueOf(captchaCode.getId()));
        RegisterUserResponse registerUserResponse = RequestHelper.postRequestJson(resourceUrl,
                                                                    resourceJson,
                                                                    UserDataRequest.class,
                                                                    RegisterUserResponse.class,
                                                                    RequestHelper::assertResponseOkAndContentTypeJson,
                                                                    testRestTemplate);
        assertNotNull(registerUserResponse);
        assertThat(registerUserResponse.isResult(), equalTo(false));

        long usersCountAfterReg = userRepository.count();
        assertThat(usersCountAfterReg, equalTo(usersCountBeforeReg));

        Map<String, String> errors = registerUserResponse.getErrors();
        assertNotNull(errors);
        String captchaError = errors.get("captcha");
        assertThat(captchaError, equalTo("Код с картинки введён неверно"));
    }

    private CaptchaCode generateAndSaveCaptchaCode() {
        CaptchaCode captchaCode = captchaCodeRepository.save(testDataGenerator.generateCaptchaCode());
        captchaCodeRepository.flush();
        return captchaCode;
    }
}