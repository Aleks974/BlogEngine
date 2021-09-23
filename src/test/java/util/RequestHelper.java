package util;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestHelper {
    public static  <T> T getRequest(String resourceUrl,
                             Class<T> responseClazz,
                             AssertFunctional assertFunction,
                             TestRestTemplate testRestTemplate) throws IOException {
        RestTemplateBuilder builder = new RestTemplateBuilder()
                                            .additionalMessageConverters(testRestTemplate.getRestTemplate().getMessageConverters());
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate(builder);

        ResponseEntity<T> response = testRestTemplateLocal.getForEntity(resourceUrl, responseClazz);
        assertFunction.assertFunc(response);

        return response.getBody();
    }

    public static <T, V> V postRequestJson(String resourceUrl,
                                           String resourceJson,
                                           Class<T> requestClazz,
                                           Class<V> responseClazz,
                                           AssertFunctional assertFunction,
                                           TestRestTemplate testRestTemplate) throws IOException {
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .additionalMessageConverters(testRestTemplate.getRestTemplate().getMessageConverters());
        TestRestTemplate testRestTemplateLocal = new TestRestTemplate(builder);

        T resource = retrieveResourceFromJson(resourceJson, requestClazz);
        HttpEntity<T> request = new HttpEntity<>(resource);
        ResponseEntity<V> response = testRestTemplateLocal.postForEntity(resourceUrl, request, responseClazz);
        assertFunction.assertFunc(response);

        return response.getBody();
    }

    public static <T> void assertResponseOkAndContentTypeJson(ResponseEntity<T> response) {
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertTrue(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
    }

    public static <T> T retrieveResourceFromJson(String json, Class<T> requestClazz) throws IOException {
        JsonParseHelper helper = new JsonParseHelper();
        return helper.retrieveResourceFromJson(json, requestClazz);
    }

}
