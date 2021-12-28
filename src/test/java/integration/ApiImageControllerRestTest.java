package integration;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.repository.CachedSettingsRepository;
import diplom.blogengine.service.util.TimestampHelper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.nio.file.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ApiImageControllerRestTest extends ApiControllerRestTest {
    @Autowired
    CachedSettingsRepository settingsRepository;

    @Autowired
    TimestampHelper timestampHelper;

/*
    @AfterEach
    public void tearDown() throws IOException {
        clearTmpUploadDir();
    }
*/

    // /api/image

    @Test
    public void givenNotAuth_whenSendPostImage_then401Unauthorized() throws Exception  {
        String notAuth = "";
        String extension = "jpg";
        int fileSize = 1024 * 1024;
        Path tmpFile = testDataGenerator.createTempFile(extension, fileSize);

        ResponseEntity<String> responseEntity = sendPostFile(tmpFile, notAuth);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

        clearTmpFile(tmpFile);
        clearTmpUploadDir();
    }

    @Test
    public void givenUserLoginAndFileWithWrongSize_whenSendPostImage_then400BadRequest() throws Exception  {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        String extension = "jpg";
        int fileSize = 6 * 1024 * 1024;
        Path tmpFile = testDataGenerator.createTempFile(extension, fileSize);

        /*assertThrows(ResourceAccessException.class, () ->{
            ResponseEntity<String> responseEntity = sendPostFile(tmpFile, cookie);
            assertNotNull(responseEntity);
            assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
            System.out.println(responseEntity.getBody());
        });*/

        ResponseEntity<String> responseEntity = sendPostFile(tmpFile, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        clearTmpFile(tmpFile);
        clearTmpUploadDir();

    }

    @Test
    public void givenUserLoginAndFileWithWrongExt_whenSendPostImage_then400BadRequest() throws Exception  {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        String extension = "pdf";
        int fileSize = 1024 * 1024;
        Path tmpFile = testDataGenerator.createTempFile(extension, fileSize);
        System.out.println(tmpFile);
        ResponseEntity<String> responseEntity = sendPostFile(tmpFile, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        System.out.println(responseEntity.getBody());
        clearTmpFile(tmpFile);
        clearTmpUploadDir();
    }

    @Test
    public void givenUserLoginAndFile_whenSendPostImage_then200Ok() throws Exception  {
        String cookie = getCookieAfterSuccessLogin(user3Email, user3Pass);
        String extension = "jpg";
        int fileSize = 1024 * 1024;
        Path tmpFile = testDataGenerator.createTempFile(extension, fileSize);

        ResponseEntity<String> responseEntity = sendPostFile(tmpFile, cookie);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        System.out.println(responseEntity.getBody());
        assertNotNull(responseEntity.getBody());

        byte[] expected = Files.readAllBytes(tmpFile);
        byte[] uploadedFileBytes = testRestTemplate.getForObject(host + responseEntity.getBody(), byte[].class);
        assertTrue(Arrays.equals(expected, uploadedFileBytes));

        clearTmpFile(tmpFile);
        clearTmpUploadDir();
    }

    // NOT TESTS //////////////////////////////////////////////////////////////////////////

    private ResponseEntity<String> sendPostFile(Path file, String cookie) {
        String resourceUrl = "/api/image";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(host)
                .path(resourceUrl)
                .build()
                .toUri();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        String fileField = "image";
        body.add(fileField, new FileSystemResource(file));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Cookie", cookie);
        HttpEntity entity = new HttpEntity<>(body, headers);

        TestRestTemplate testRestTemplateLocal = new TestRestTemplate();
        return testRestTemplateLocal.postForEntity(uri, entity, String.class);
    }


}
