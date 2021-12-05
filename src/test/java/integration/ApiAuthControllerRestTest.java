package integration;

import diplom.blogengine.api.request.UserNewPasswordRequest;
import diplom.blogengine.api.request.UserRegisterDataRequest;
import diplom.blogengine.api.request.UserResetPasswordRequest;
import diplom.blogengine.api.response.AuthResponse;
import diplom.blogengine.api.response.CaptchaResponse;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.UserInfoAuthResponse;
import diplom.blogengine.model.*;
import diplom.blogengine.repository.*;
import diplom.blogengine.service.util.MailHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ApiAuthControllerRestTest extends ApiControllerRestTest {
    @Autowired
    private CaptchaCodeRepository captchaCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private CachedSettingsRepository cachedSettingsRepository;

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    @MockBean
    private MailHelper mailHelper;

    // /api/auth/login

    @Test
    public void givenBadCredentialsWhileLogin_whenSendPostLogin_thenResultFalse() throws Exception {
        String wrongEmail = "wrong@rr.ru";
        loginUserFailAndAssert(wrongEmail, user3Pass);

        String wrongPass = "wrongpassword";
        loginUserFailAndAssert(user3Email, wrongPass);
    }

    @Test
    public void givenCorrectCredentialsWhileLogin_whenSendPostLogin_thenResultTrueAndCookie() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        assertNotNull(cookie);
    }

    // /api/auth/check

    @Test
    public void givenNotAuth_whenSendGetAuthCheck_thenResultFalse() throws Exception {
        String notAuth = "";
        ResponseEntity<AuthResponse> responseEntity = sendGetAuthCheck(notAuth);
        assertStatusOkAndContentTypeJson(responseEntity);

        AuthResponse authResponse = responseEntity.getBody();
        boolean expectedResultField = false;
        assertEquals(expectedResultField, authResponse.isResult());
        assertNull(authResponse.getUser());
    }

    @Test
    public void giveUserLogin_whenSendGetAuthCheck_thenResultTrue() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        ResponseEntity<AuthResponse> responseEntity = sendGetAuthCheck(cookie);
        assertStatusOkAndContentTypeJson(responseEntity);

        AuthResponse authResponse = responseEntity.getBody();
        boolean expectedResultField = true;
        assertEquals(expectedResultField, authResponse.isResult());

        UserInfoAuthResponse user = authResponse.getUser();
        assertNotNull(user);
        System.out.println(user);

        String expectedName = "Ivan Egorov";
        assertEquals(expectedName, user.getName());
        assertNull(user.getPhoto());
        String expectedEmail = "c@cc.ru";
        assertEquals(expectedEmail, user.getEmail());
        boolean expecteMod = false;
        assertEquals(expecteMod, user.isModeration());
        int expectedModCount = 0;
        assertEquals(expectedModCount, user.getModerationCount());
        boolean expectedSet = false;
        assertEquals(expectedSet, user.isSettings());
    }

    @Test
    public void giveModeratorLogin_whenSendGetAuthCheck_thenResultTrue() throws Exception {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);

        ResponseEntity<AuthResponse> responseEntity = sendGetAuthCheck(cookie);
        assertStatusOkAndContentTypeJson(responseEntity);

        AuthResponse authResponse = responseEntity.getBody();
        boolean expectedResultField = true;
        assertEquals(expectedResultField, authResponse.isResult());

        UserInfoAuthResponse user = authResponse.getUser();
        assertNotNull(user);
        System.out.println(user);

        String expectedName = "Aleksandr Ivanov";
        assertEquals(expectedName, user.getName());
        assertNull(user.getPhoto());
        String expectedEmail = "a@aa.ru";
        assertEquals(expectedEmail, user.getEmail());
        boolean expecteMod = true;
        assertEquals(expecteMod, user.isModeration());
        int expectedModCount = 2;
        assertEquals(expectedModCount, user.getModerationCount());
        boolean expectedSet = true;
        assertEquals(expectedSet, user.isSettings());
    }

    // /api/auth/captcha

    @Test
    public void givenResourceUrl_whenSendGetCaptcha_thenCaptchaSavedAndReturnedCorrect() throws Exception {
        String resourceUrl = "/api/auth/captcha";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<CaptchaResponse> responseEntity = testRestTemplate.getForEntity(uri, CaptchaResponse.class);
        assertStatusOkAndContentTypeJson(responseEntity);

        CaptchaResponse response = responseEntity.getBody();
        assertNotNull(response);
        String IMAGE_CONTENT_TYPE = "data:image/png;base64";
        assertTrue(response.getImage().startsWith(IMAGE_CONTENT_TYPE));
        assertNotNull(captchaCodeRepository.findCodeBySecret(response.getSecret()));
    }

    // /api/auth/register

    @Test
    public void givenUserRegisterRequestAndWrongCaptchaCode_whenSendPostRegister_then400BadRequestUserNotSaved() throws Exception {
        cachedSettingsRepository.clearAllCache();
        setMultiuserMode(true);

        CaptchaCode captcha = generateAndSaveCaptchaCode();
        String wrongCode = "wrong";
        UserRegisterDataRequest request = testDataGenerator.generateUserRegisterDataRequest(wrongCode, captcha.getSecretCode());

        ResponseEntity<ResultResponse> responseEntity = sendPostUserRegister(request);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        ResultResponse response = responseEntity.getBody();
        assertFalse(response.getResult());

        assertNotNull(response.getErrors());
        String errorKey = "captcha";
        assertTrue(response.getErrors().containsKey(errorKey));

        long expectedCount = 3;
        assertEquals(expectedCount, userRepository.count());

        String expectedEmail = request.getEmail();
        assertFalse(userRepository.findByEmail(expectedEmail).isPresent());
    }


    @Test
    public void givenWrongUserRegisterRequest_whenSendPostRegister_then400BadRequestUserNotSaved() throws Exception {
        CaptchaCode captcha = generateAndSaveCaptchaCode();

        UserRegisterDataRequest request = testDataGenerator.generateUserRegisterDataRequest();
        ResponseEntity<ResultResponse> responseEntity = sendPostUserRegister(request);
        assertBadRequestAndResultFalse(responseEntity, errors -> {
            Set<String> expectedErrorKeys = Set.of("email", "password", "name", "captcha", "captchaSecret");
            assertEquals(expectedErrorKeys, errors.keySet());
        });


        request = testDataGenerator.generateUserRegisterDataRequest(captcha.getCode(), captcha.getSecretCode());
        String wrongEmail = "test.ru";
        request.setEmail(wrongEmail);
        responseEntity = sendPostUserRegister(request);
        assertBadRequestAndResultFalse(responseEntity, errors -> {
            Set<String> expectedErrorKeys = Set.of("email");
            assertEquals(expectedErrorKeys, errors.keySet());
        });


        request = testDataGenerator.generateUserRegisterDataRequest(captcha.getCode(), captcha.getSecretCode());
        String existedEmail = "a@aa.ru";
        request.setEmail(existedEmail);
        responseEntity = sendPostUserRegister(request);
        assertBadRequestAndResultFalse(responseEntity, errors -> {
            Set<String> expectedErrorKeys = Set.of("email");
            assertEquals(expectedErrorKeys, errors.keySet());
        });


        request = testDataGenerator.generateUserRegisterDataRequest(captcha.getCode(), captcha.getSecretCode());
        String shortPass = "12345";
        request.setPassword(shortPass);
        responseEntity = sendPostUserRegister(request);
        assertBadRequestAndResultFalse(responseEntity, errors -> {
            Set<String> expectedErrorKeys = Set.of("password");
            assertEquals(expectedErrorKeys, errors.keySet());
        });


        request = testDataGenerator.generateUserRegisterDataRequest(captcha.getCode(), captcha.getSecretCode());
        String wrongName = "123";
        request.setName(wrongName);
        responseEntity = sendPostUserRegister(request);
        assertBadRequestAndResultFalse(responseEntity, errors -> {
            Set<String> expectedErrorKeys = Set.of("name");
            assertEquals(expectedErrorKeys, errors.keySet());
        });

        long expectedUsersCount = 3;
        assertEquals(expectedUsersCount, userRepository.count());
    }

    @Test
    public void givenUserLoginAndUserRegisterRequestAndCaptcha_whenSendPostRegister_then404NotFound() throws Exception {
        CaptchaCode captcha = generateAndSaveCaptchaCode();
        UserRegisterDataRequest request = testDataGenerator.generateUserRegisterDataRequest(captcha.getCode(), captcha.getSecretCode());

        ResponseEntity<ResultResponse> responseEntity = sendPostUserRegister(request);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse response = responseEntity.getBody();
        assertTrue(response.getResult());

        long expectedCount = 4;
        assertEquals(expectedCount, userRepository.count());

        String expectedEmail = request.getEmail();
        assertTrue(userRepository.findByEmail(expectedEmail).isPresent());
    }


    @Test
    public void givenUserRegisterRequestAndMultiuserModeIsOff_whenSendPostRegister_then404AndUserNotSaved() throws Exception {
        cachedSettingsRepository.clearAllCache();
        setMultiuserMode(false);

        System.out.println(settingsRepository.findByCode(SettingsCode.MULTIUSER_MODE));

        CaptchaCode captcha = generateAndSaveCaptchaCode();
        UserRegisterDataRequest request = testDataGenerator.generateUserRegisterDataRequest(captcha.getCode(), captcha.getSecretCode());

        ResponseEntity<ResultResponse> responseEntity = sendPostUserRegister(request);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

        long expectedCount = 3;
        assertEquals(expectedCount, userRepository.count());
    }



    @Test
    public void givenUserRegisterRequestAndCaptcha_whenSendPostRegister_thenUserSaved() throws Exception {
        cachedSettingsRepository.clearAllCache();
        setMultiuserMode(true);

        CaptchaCode captcha = generateAndSaveCaptchaCode();
        UserRegisterDataRequest request = testDataGenerator.generateUserRegisterDataRequest(captcha.getCode(), captcha.getSecretCode());

        ResponseEntity<ResultResponse> responseEntity = sendPostUserRegister(request);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse response = responseEntity.getBody();
        assertTrue(response.getResult());

        long expectedCount = 4;
        assertEquals(expectedCount, userRepository.count());

        String expectedEmail = request.getEmail();
        assertTrue(userRepository.findByEmail(expectedEmail).isPresent());
    }

    // /api/auth/restore

    @Test
    public void givenResetPasswordRequestWithNotExistedEmail_whenSendPostRestore_then200OkResultFalse() throws Exception {
        String wrongEmail = "notexisted@email.ru";
        UserResetPasswordRequest request = new UserResetPasswordRequest(wrongEmail);

        ResponseEntity<ResultResponse> responseEntity = sendPostAuthRestore(request);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse response = responseEntity.getBody();
        assertFalse(response.getResult());
    }

    @Test
    public void givenResetPasswordRequestWithNullEmail_whenSendPostRestore_then400BadRequest() throws Exception {
        String wrongEmail = null;
        UserResetPasswordRequest request = new UserResetPasswordRequest(wrongEmail);

        ResponseEntity<ResultResponse> responseEntity = sendPostAuthRestore(request);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLoginAndResetPasswordRequest_whenSendPostRestore_then404NotFound() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        UserResetPasswordRequest request = new UserResetPasswordRequest(user3Email);

        ResponseEntity<ResultResponse> responseEntity = sendPostAuthRestore(request, cookie);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }


    @Test
    public void givenResetPasswordRequest_whenSendPostRestore_then200OkPasswordResetTokenCreated() throws Exception {
        assertTrue(passwordTokenRepository.findAll().isEmpty());
        sendRestorePasswordRequestAndAssert(user3Email);
        String token = getPasswordResetToken(user3Id);
        assertNotNull(token);
    }

    // /api/auth/password

    @Test
    public void givenCaptchaAndResetPasswordTokenAndNewPasswordRequest_whenSendPostNewPassword_then200OkNewPasswordSaved() throws Exception {
        CaptchaCode captcha = generateAndSaveCaptchaCode();

        assertTrue(passwordTokenRepository.findAll().isEmpty());
        sendRestorePasswordRequestAndAssert(user3Email);
        String token = getPasswordResetToken(user3Id);
        assertNotNull(token);

        String newPass = "newpassword";
        String codeCaptcha = captcha.getCode();
        String captchaSecret = captcha.getSecretCode();
        UserNewPasswordRequest request = UserNewPasswordRequest
                                        .builder()
                                        .code(token)
                                        .password(newPass)
                                        .captcha(codeCaptcha)
                                        .captchaSecret(captchaSecret)
                                        .build();
        ResponseEntity<ResultResponse> responseEntity = sendPostNewPassword(request);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResultResponse response = responseEntity.getBody();
        assertTrue(response.getResult());

        loginUserAndAssert(user3Email, newPass);
    }


    @Test
    public void givenWrongCaptchaAndResetPasswordTokenAndNewPasswordRequest_whenSendPostNewPassword_then400BadRequestAndPassNotSaved() throws Exception {
        CaptchaCode captcha = generateAndSaveCaptchaCode();

        assertTrue(passwordTokenRepository.findAll().isEmpty());
        sendRestorePasswordRequestAndAssert(user3Email);
        String token = getPasswordResetToken(user3Id);
        assertNotNull(token);

        String newPass = "newpassword";
        String codeCaptcha = "wrongCaptcha";
        String captchaSecret = captcha.getSecretCode();
        UserNewPasswordRequest request = UserNewPasswordRequest
                .builder()
                .code(token)
                .password(newPass)
                .captcha(codeCaptcha)
                .captchaSecret(captchaSecret)
                .build();
        ResponseEntity<ResultResponse> responseEntity = sendPostNewPassword(request);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ResultResponse response = responseEntity.getBody();
        assertFalse(response.getResult());
        assertNotNull(response.getErrors());

        loginUserFailAndAssert(user3Email, newPass);
    }

    @Test
    public void givenWrongPassAndResetPasswordTokenAndNewPasswordRequest_whenSendPostNewPassword_then200OkNewPasswordSaved() throws Exception {
        CaptchaCode captcha = generateAndSaveCaptchaCode();

        assertTrue(passwordTokenRepository.findAll().isEmpty());
        sendRestorePasswordRequestAndAssert(user3Email);
        String token = getPasswordResetToken(user3Id);
        assertNotNull(token);

        String newPass = "short";
        String codeCaptcha = captcha.getCode();
        String captchaSecret = captcha.getSecretCode();
        UserNewPasswordRequest request = UserNewPasswordRequest
                .builder()
                .code(token)
                .password(newPass)
                .captcha(codeCaptcha)
                .captchaSecret(captchaSecret)
                .build();
        ResponseEntity<ResultResponse> responseEntity = sendPostNewPassword(request);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ResultResponse response = responseEntity.getBody();
        assertFalse(response.getResult());
        assertNotNull(response.getErrors());

        loginUserFailAndAssert(user3Email, newPass);
    }

    @Test
    public void givenWrongTokenAndNewPasswordRequest_whenSendPostNewPassword_then400BadRequestAndPassNotSaved() throws Exception {
        CaptchaCode captcha = generateAndSaveCaptchaCode();

        String token = "wrongToken";

        String newPass = "newpassword";
        String codeCaptcha = captcha.getCode();
        String captchaSecret = captcha.getSecretCode();
        UserNewPasswordRequest request = UserNewPasswordRequest
                .builder()
                .code(token)
                .password(newPass)
                .captcha(codeCaptcha)
                .captchaSecret(captchaSecret)
                .build();
        ResponseEntity<ResultResponse> responseEntity = sendPostNewPassword(request);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ResultResponse response = responseEntity.getBody();
        assertFalse(response.getResult());
        assertNotNull(response.getErrors());

        loginUserFailAndAssert(user3Email, newPass);
    }

    @Test
    public void givenExpiredTokenAndNewPasswordRequest_whenSendPostNewPassword_then400BadRequestAndPassNotSaved() throws Exception {
        CaptchaCode captcha = generateAndSaveCaptchaCode();

        String token = createExpiredToken(user3Id);

        String newPass = "newpassword";
        String codeCaptcha = captcha.getCode();
        String captchaSecret = captcha.getSecretCode();
        UserNewPasswordRequest request = UserNewPasswordRequest
                .builder()
                .code(token)
                .password(newPass)
                .captcha(codeCaptcha)
                .captchaSecret(captchaSecret)
                .build();
        ResponseEntity<ResultResponse> responseEntity = sendPostNewPassword(request);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ResultResponse response = responseEntity.getBody();
        assertFalse(response.getResult());
        assertNotNull(response.getErrors());

        loginUserFailAndAssert(user3Email, newPass);
    }

    // NOT TESTS ///////////////////////////////////////////////////////////////////////////

    private void assertStatusOkAndContentTypeJson(ResponseEntity<?> responseEntity) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
    }

    private ResponseEntity<AuthResponse> sendGetAuthCheck(String cookie) {
        String resourceUrl = "/api/auth/check";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);
        HttpEntity entity = new HttpEntity(headers);
        return testRestTemplate.exchange(uri, HttpMethod.GET, entity, AuthResponse.class);
    }

    private ResponseEntity<ResultResponse> sendPostUserRegister(UserRegisterDataRequest request, String...cookie) {
        String resourceUrl = "/api/auth/register";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        HttpEntity<UserRegisterDataRequest> entity;
        if (cookie != null && cookie.length == 1) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Cookie", cookie[0]);
            entity = new HttpEntity<>(request, httpHeaders);
        } else {
            entity = new HttpEntity<>(request);
        }
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        return testRestTemplate.exchange(uri, HttpMethod.POST, entity, ResultResponse.class);
    }

    private void assertBadRequestAndResultFalse(ResponseEntity<ResultResponse> responseEntity, Consumer<Map<String, String>> errorsConsumer) {
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ResultResponse response = responseEntity.getBody();
        assertFalse(response.getResult());
        assertNotNull(response.getErrors());

        if (errorsConsumer != null) {
            errorsConsumer.accept(response.getErrors());
        }
    }

    private ResponseEntity<ResultResponse> sendPostAuthRestore(UserResetPasswordRequest request) {
        return sendPostAuthRestore(new HttpEntity<>(request));
    }

    private ResponseEntity<ResultResponse> sendPostAuthRestore(UserResetPasswordRequest request, String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);
        return sendPostAuthRestore(new HttpEntity<>(request, headers));
    }

    private ResponseEntity<ResultResponse> sendPostAuthRestore(HttpEntity<?> entity) {
        String resourceUrl = "/api/auth/restore";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        return testRestTemplate.exchange(uri, HttpMethod.POST, entity, ResultResponse.class);
    }

    private ResponseEntity<ResultResponse> sendPostNewPassword(UserNewPasswordRequest request) {
        String resourceUrl = "/api/auth/password";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        HttpEntity<UserNewPasswordRequest> entity = new HttpEntity<>(request);
        return testRestTemplate.exchange(uri, HttpMethod.POST, entity, ResultResponse.class);
    }

    private void sendRestorePasswordRequestAndAssert(String email) {
        UserResetPasswordRequest request = new UserResetPasswordRequest(email);
        ResponseEntity<ResultResponse> responseEntity = sendPostAuthRestore(request);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResultResponse response = responseEntity.getBody();
        assertTrue(response.getResult());
    }

    private String getPasswordResetToken(long userId) {
       /*String sql = "SELECT * FROM password_reset_tokens WHERE user_id = ?";
        PasswordResetToken token = (PasswordResetToken) entityManager.createNativeQuery(sql)
                                    .setParameter(1, userId)
                                    .getSingleResult();*/
        String jpqlQuery = "SELECT t FROM PasswordResetToken t WHERE t.user.id = ?1";
        PasswordResetToken token = entityManager.createQuery(jpqlQuery, PasswordResetToken.class)
                .setParameter(1, userId)
                .getSingleResult();
        assertNotNull(token);
        return token.getToken();
    }

    private void sendNewPasswordRequestAndAssert(UserNewPasswordRequest request) {
        ResponseEntity<ResultResponse> responseEntity = sendPostNewPassword(request);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ResultResponse response = responseEntity.getBody();
        assertTrue(response.getResult());
    }

    private CaptchaCode generateAndSaveCaptchaCode() {
        CaptchaCode captchaCode = captchaCodeRepository.saveAndFlush(testDataGenerator.generateCaptchaCode());
        return captchaCode;
    }


    private void setMultiuserMode(boolean isOn) {
        GlobalSetting setting = settingsRepository.findByCode(SettingsCode.MULTIUSER_MODE);
        final String VALUE = isOn ? "YES" : "NO";
        setting.setValue(VALUE);
        settingsRepository.saveAndFlush(setting);
    }


    private String createExpiredToken(long userId) {
        User user = userRepository.findById(userId).get();

        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordResetToken.setExpiryDate(LocalDateTime.now().minus(1, ChronoUnit.HOURS));
        passwordTokenRepository.save(passwordResetToken);
        return token;
    }
}
