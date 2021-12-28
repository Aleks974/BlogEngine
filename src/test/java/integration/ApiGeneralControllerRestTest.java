package integration;

import diplom.blogengine.api.request.GlobalSettingsRequest;
import diplom.blogengine.api.request.UserProfileDataRequest;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.StatisticsResponse;
import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.model.GlobalSetting;
import diplom.blogengine.model.SettingsCode;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.CachedSettingsRepository;
import diplom.blogengine.service.util.TimestampHelper;
import diplom.blogengine.service.util.UriHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ApiGeneralControllerRestTest extends ApiControllerRestTest {
    @Autowired
    CachedSettingsRepository settingsRepository;

    @Autowired
    TimestampHelper timestampHelper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    BlogSettings blogSettings;

    // /api/settings

    @Test
    public void givenNotAuth_whenSendPostGlobalSettings_then401Unauthorized() {
        String notAuth = "";
        GlobalSettingsRequest request = testDataGenerator.genGlobalSettingsRequest();

        ResponseEntity<ResultResponse> responseEntity = sendPutGlobalSettings(request, notAuth);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenAuthUserAndNotModerator_whenSendPostGlobalSettings_then401Unauthorized()  {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        GlobalSettingsRequest request = testDataGenerator.genGlobalSettingsRequest();

        ResponseEntity<ResultResponse> responseEntity = sendPutGlobalSettings(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenAuthModerator_whenSendPostGlobalSettings_then200Ok()  {
        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);
        GlobalSettingsRequest request = testDataGenerator.genGlobalSettingsRequest();

        ResponseEntity<ResultResponse> responseEntity = sendPutGlobalSettings(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        assertTrue(responseEntity.getBody().getResult());

        String updatedValue = "NO";
        List<GlobalSetting> settings = settingsRepository.findAll();
        assertTrue(settings.stream().allMatch(gs -> gs.getValue().equals(updatedValue)));
    }

    // /api/statistics/my

    @Test
    public void givenNotAuth_whenSendGetStatisticsMy_then401Unauthorized() {
        String notAuth = "";
        ResponseEntity<StatisticsResponse> responseEntity = sendGetStatisticsMy(notAuth);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenLoginUser_whenSendGetStatisticsMy_then200Ok() throws IOException {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        ResponseEntity<StatisticsResponse> responseEntity = sendGetStatisticsMy(cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        StatisticsResponse response = responseEntity.getBody();
        long expectedPostsCount = 2;
        assertEquals(expectedPostsCount, response.getPostsCount());
        long expectedLikesCount = 0;
        assertEquals(expectedLikesCount, response.getLikesCount());
        long expectedDislikesCount = 0;
        assertEquals(expectedDislikesCount, response.getDislikesCount());
        long expectedViewsCount = 1;
        assertEquals(expectedViewsCount, response.getViewsCount());

        LocalDateTime dateTime = LocalDateTime.parse("2021-08-11 13:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long expectedFirstPublication = timestampHelper.toTimestampAtServerZone(dateTime);
        assertEquals(expectedFirstPublication, response.getFirstPublication());
    }

    // /api/statistics/all

    @Test
    public void givenStatisticIsPublic_whenSendGetStatisticsAll_then200Ok() throws Exception  {
        GlobalSetting setting = settingsRepository.findByCode(SettingsCode.STATISTICS_IS_PUBLIC);
        final String YES_VALUE = "YES";
        setting.setValue(YES_VALUE);
        settingsRepository.saveAndFlush(setting);
        settingsRepository.clearAllCache();

        String notAuth = "";
        ResponseEntity<StatisticsResponse> responseEntity = sendGetStatisticsAll(notAuth);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        StatisticsResponse response = responseEntity.getBody();
        assertStatisticsResponseAll(response);
    }

    @Test
    public void givenStatisticIsNotPublicAndNotAuth_whenSendGetStatisticsAll_then401Unauthorized()  {
        GlobalSetting setting = settingsRepository.findByCode(SettingsCode.STATISTICS_IS_PUBLIC);
        final String NO_VALUE = "NO";
        setting.setValue(NO_VALUE);
        settingsRepository.saveAndFlush(setting);
        settingsRepository.clearAllCache();

        String notAuth = "";
        ResponseEntity<StatisticsResponse> responseEntity = sendGetStatisticsAll(notAuth);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenStatisticIsNotPublicAndUserLoginAndNotModerator_whenSendGetStatisticsAll_then401Unauthorized()  {
        GlobalSetting setting = settingsRepository.findByCode(SettingsCode.STATISTICS_IS_PUBLIC);
        final String NO_VALUE = "NO";
        setting.setValue(NO_VALUE);
        settingsRepository.saveAndFlush(setting);
        settingsRepository.clearAllCache();

        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        ResponseEntity<StatisticsResponse> responseEntity = sendGetStatisticsAll(cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenStatisticIsNotPublicAndModeratorLogin_whenSendGetStatisticsAll_then200Ok()  {
        GlobalSetting setting = settingsRepository.findByCode(SettingsCode.STATISTICS_IS_PUBLIC);
        final String NO_VALUE = "NO";
        setting.setValue(NO_VALUE);
        settingsRepository.saveAndFlush(setting);
        settingsRepository.clearAllCache();

        String cookie = getCookieAfterSuccessLogin(moderatorEmail, moderatorPass);
        ResponseEntity<StatisticsResponse> responseEntity = sendGetStatisticsAll(cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        StatisticsResponse response = responseEntity.getBody();
        assertStatisticsResponseAll(response);
    }

    // /api/profile/my

    @Test
    public void givenNotAuth_whenSendPostSaveProfile_then401Unauthorized() {
        String notAuth = "";
        String newName = "New Name";
        UserProfileDataRequest request = testDataGenerator.genUserProfileDataRequest(req -> {
            req.setName(newName);
        });
        ResponseEntity<ResultResponse> responseEntity = sendPostSaveProfileMy(request, notAuth);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void givenUserLoginAndIncorrectRequestData_whenSendPostSaveProfile_then400BadRequest() {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        // wrong name
        String wrongName = "*wrongName";
        UserProfileDataRequest request = testDataGenerator.genUserProfileDataRequest(req -> req.setName(wrongName));
        ResponseEntity<ResultResponse> responseEntity = sendPostSaveProfileMy(request, cookie);
        assertResultResponseBadRequest(responseEntity);

        // wrong email
        String wrongEmail = "wrongemail.ru";
        request = testDataGenerator.genUserProfileDataRequest(req -> req.setEmail(wrongEmail));
        responseEntity = sendPostSaveProfileMy(request, cookie);
        assertResultResponseBadRequest(responseEntity);

        // wrong pass
        String wrongPass = "short";
        request = testDataGenerator.genUserProfileDataRequest(req -> req.setPassword(wrongPass));
        responseEntity = sendPostSaveProfileMy(request, cookie);
        assertResultResponseBadRequest(responseEntity);

        // wrong removePhoto
        int wrongRemovePhoto = 2;
        request = testDataGenerator.genUserProfileDataRequest(req -> req.setRemovePhoto(wrongRemovePhoto));
        responseEntity = sendPostSaveProfileMy(request, cookie);
        assertResultResponseBadRequest(responseEntity);
    }


    @Test
    public void givenUserLoginAndRequestWithNameEmail_whenSendPostSaveProfile_then200Ok() {
        String newName = "New Name";
        String newEmail = "d@dd.ru";
        String newPass = "password3";
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        UserProfileDataRequest request = testDataGenerator.genUserProfileDataRequest(req -> {
            req.setName(newName);
            req.setEmail(newEmail);
            req.setPassword(newPass);
        });
        ResponseEntity<ResultResponse> responseEntity = sendPostSaveProfileMy(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse response = responseEntity.getBody();
        assertTrue(response.getResult());

        User updatedUser = entityManager.find(User.class, user3Id);
        assertEquals(newName, updatedUser.getName());
        assertEquals(newEmail, updatedUser.getEmail());

        getCookieAfterSuccessLogin(newEmail, newPass);
    }

    @Test
    public void givenUserLoginAndPhotoFileAndNewName_whenSendPostSaveProfileForm_then200Ok() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        Path tmpPhoto = testDataGenerator.createPhotoFile("jpg", 500, 500);
        body.add("photo", new FileSystemResource(tmpPhoto));
        String newName = "New Name";
        body.add("name", newName);

        ResponseEntity<ResultResponse> responseEntity = sendPostSaveProfileMyForm(body, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ResultResponse response = responseEntity.getBody();
        assertTrue(response.getResult());

        User updatedUser = entityManager.find(User.class, user3Id);
        assertEquals(newName, updatedUser.getName());
        String photo = updatedUser.getPhoto();
        assertNotNull(photo);

        System.out.println(photo);
       /* if (photo.startsWith("/")) {
            photo = photo.substring(1);
        }*/
        Path uploadDirRoot = Path.of(Objects.requireNonNull(blogSettings.getUploadDir(), "uploadDir is null"));
        String uploadUrlPrefix = Objects.requireNonNull(blogSettings.getUploadUrlPrefix(), "uploadUrlPrefix is null");
        Path photoPath = UriHelper.localPathFromUri(photo, uploadDirRoot, uploadUrlPrefix);
        assertTrue(Files.exists(photoPath));

        clearTmpFile(tmpPhoto);
        clearTmpUploadDir();
    }

    @Test
    public void givenUserLoginAndSavePhotoFileAndRemovePhotoRequest_whenSendPostSaveProfile_then200OkAndPhotoRemoved() throws Exception {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        Path tmpPhoto = testDataGenerator.createPhotoFile("jpg", 500, 500);
        body.add("photo", new FileSystemResource(tmpPhoto));

        ResponseEntity<ResultResponse> responseEntity = sendPostSaveProfileMyForm(body, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().getResult());

        User updatedUser = entityManager.find(User.class, user3Id);
        assertNotNull(updatedUser.getPhoto());

        // remove photo request
        UserProfileDataRequest request = testDataGenerator.genUserProfileDataRequest(req -> {
            req.setRemovePhoto(1);
        });
        responseEntity = sendPostSaveProfileMy(request, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().getResult());

        updatedUser = entityManager.find(User.class, user3Id);
        assertNull(updatedUser.getPhoto());

        clearTmpFile(tmpPhoto);
        clearTmpUploadDir();
    }


    // NOT TESTS //////////////////////////////////////////////////////////////////////////

    private void assertStatisticsResponseAll(StatisticsResponse response) {
        long expectedPostsCount = 3;
        assertEquals(expectedPostsCount, response.getPostsCount());
        long expectedLikesCount = 1;
        assertEquals(expectedLikesCount, response.getLikesCount());
        long expectedDislikesCount = 1;
        assertEquals(expectedDislikesCount, response.getDislikesCount());
        long expectedViewsCount = 2;
        assertEquals(expectedViewsCount, response.getViewsCount());

        LocalDateTime dateTime = LocalDateTime.parse("2021-08-11 13:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long expectedFirstPublication = timestampHelper.toTimestampAtServerZone(dateTime);
        assertEquals(expectedFirstPublication, response.getFirstPublication());
    }

    private ResponseEntity<StatisticsResponse> sendGetStatisticsMy(String cookie) {
        String resourceUrl = "/api/statistics/my";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);
        HttpEntity entity = new HttpEntity<>(headers);
        return testRestTemplateLocal.exchange(uri, HttpMethod.GET, entity, StatisticsResponse.class);
    }


    private ResponseEntity<StatisticsResponse> sendGetStatisticsAll(String cookie) {
        String resourceUrl = "/api/statistics/all";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);
        HttpEntity entity = new HttpEntity<>(headers);
        return testRestTemplateLocal.exchange(uri, HttpMethod.GET, entity, StatisticsResponse.class);
    }

    private ResponseEntity<ResultResponse> sendPutGlobalSettings(GlobalSettingsRequest request, String cookie) {
        String resourceUrl = "/api/settings";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);
        HttpEntity<GlobalSettingsRequest> entity = new HttpEntity<>(request, headers);
        return testRestTemplateLocal.exchange(uri, HttpMethod.PUT, entity, ResultResponse.class);

    }

    private ResponseEntity<ResultResponse> sendPostSaveProfileMy(UserProfileDataRequest request, String cookie) {
        String resourceUrl = "/api/profile/my";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", cookie);
        return testRestTemplateLocal.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, headers), ResultResponse.class);
    }


    private ResponseEntity<ResultResponse> sendPostSaveProfileMyForm(MultiValueMap<String, Object> body, String cookie) {
        String resourceUrl = "/api/profile/my";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Cookie", cookie);
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        return testRestTemplateLocal.exchange(uri, HttpMethod.POST, new HttpEntity<>(body, headers), ResultResponse.class);
    }

}
