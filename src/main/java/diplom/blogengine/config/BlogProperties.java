package diplom.blogengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
    private String hashAlgorithm;
    private int captchaDeleteTimeout;
}
