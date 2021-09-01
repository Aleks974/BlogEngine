import diplom.blogengine.Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
                classes = Application.class)
public class ApplicationContextTest {

    @Test
    public void whenLoadApplicationContext_thenOk() {

    }
}
