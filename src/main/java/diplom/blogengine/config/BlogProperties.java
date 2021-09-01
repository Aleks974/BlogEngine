package diplom.blogengine.config;

import diplom.blogengine.service.sort.SortField;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "blog")
public class BlogProperties {
    private String title;
    private String subtitle;
    private String phone;
    private String email;
    private String copyright;
    private String copyrightFrom;
    private String serverTimeZone;
    private String hashAlgorithm;;
    private final Map<String, SortField> postSortModes = new HashMap<>();
}
