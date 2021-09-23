package integration;

import diplom.blogengine.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ActiveProfiles("test")
public class IntegrationTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private String testURL;

    @BeforeEach
    public void setUp() throws Exception {
        testURL = "http://localhost:" + port + "/";
    }

    public void getIndex_thenStatusOK200() {
        ResponseEntity<String> response = testRestTemplate.getForEntity(testURL, String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);

    }
}
