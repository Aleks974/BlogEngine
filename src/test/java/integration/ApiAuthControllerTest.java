package integration;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.api.request.UserRegisterDataRequest;
import diplom.blogengine.api.response.AuthResponse;
import diplom.blogengine.api.response.CaptchaResponse;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.model.CaptchaCode;
import diplom.blogengine.repository.CaptchaCodeRepository;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.service.util.PasswordHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import util.RequestHelper;
import util.TestDataGenerator;


import javax.persistence.EntityManager;
import javax.swing.text.StyledEditorKit;

import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
        AuthResponse authResponse = RequestHelper.sendGetRequest(resourceUrl,
                                                        AuthResponse.class,
                                                        RequestHelper::assertResponseOkAndContentTypeJson,
                                                        testRestTemplate);

        assertNotNull(authResponse);
        boolean expectedResultField = false;
        boolean actualResultField = authResponse.isResult();
        assertEquals(expectedResultField, actualResultField);
    }


    @Test
    public void givenResourceUrl_whenSendGetCaptcha_thenCaptchaSavedAndReturnedCorrect() throws Exception {
        String resourceUrl = host + "/api/auth/captcha";
        CaptchaResponse captchaResponse = RequestHelper.sendGetRequest(resourceUrl,
                                                    CaptchaResponse.class,
                                                    RequestHelper::assertResponseOkAndContentTypeJson,
                                                    testRestTemplate);

        assertNotNull(captchaResponse);
        String IMAGE_CONTENT_TYPE = "data:image/png;base64";
        assertTrue(captchaResponse.getImage().startsWith(IMAGE_CONTENT_TYPE));
        assertNotNull(captchaCodeRepository.findCodeBySecret(captchaResponse.getSecret()));

    }

    @Test
    public void givenResourceUrlAndCaptcha_whenSendPostUser_thenUserRegisteredAndResponseCorrect() throws Exception {
        String resourceUrl = host + "/api/auth/register";
        long usersCountBeforeReg = userRepository.count();
        CaptchaCode captchaCode = generateAndSaveCaptchaCode();

        String resourceJson = testDataGenerator.generateUserDataRequestJson(captchaCode.getCode(), captchaCode.getSecretCode());
        ResultResponse registerUserResponse = RequestHelper.sendPostRequestJson(resourceUrl,
                                                                    resourceJson,
                                                                    UserRegisterDataRequest.class,
                                                                    ResultResponse.class,
                                                                    RequestHelper::assertResponseOkAndContentTypeJson,
                                                                    testRestTemplate);
        assertNotNull(registerUserResponse);
        assertThat(registerUserResponse.getResult().booleanValue(), equalTo(true));

        long usersCountAfterReg = userRepository.count();
        assertThat(usersCountAfterReg, equalTo(usersCountBeforeReg + 1));
    }


    @Test
    public void givenResourceUrlAndCaptcha_whenSendPostUserWithWrongCode_thenUserNotRegisteredAndResponseCorrect() throws Exception {
        String resourceUrl = host + "/api/auth/register";
        long usersCountBeforeReg = userRepository.count();
        CaptchaCode captchaCode = generateAndSaveCaptchaCode();

        String resourceJson = testDataGenerator.generateUserDataRequestJson("wrong_code", captchaCode.getSecretCode());
        ResultResponse registerUserResponse = RequestHelper.sendPostRequestJson(resourceUrl,
                                                                    resourceJson,
                                                                    UserRegisterDataRequest.class,
                                                                    ResultResponse.class,
                                                                    RequestHelper::assertResponseOkAndContentTypeJson,
                                                                    testRestTemplate);
        assertNotNull(registerUserResponse);
        assertThat(registerUserResponse.getResult().booleanValue(), equalTo(false));

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
