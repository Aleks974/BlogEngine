package diplom.blogengine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan("diplom.blogengine.config")
@EnableJpaRepositories({"diplom.blogengine.repository"})
@EntityScan("diplom.blogengine.model")
public class Application {
    public static void main(String[] args) {
        try {
            SpringApplication.run(Application.class, args);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
