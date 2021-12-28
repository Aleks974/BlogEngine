package diplom.blogengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "blog")
public class BlogProperties {
    private String title;
    private String siteUrl;
    private String subtitle;
    private String phone;
    private String email;
    private String copyright;
    private String copyrightFrom;
    private String serverTimeZone;
    private int captchaDeleteTimeout;
    private String permittedTags;
    private String uploadFilesExtensions;
    private String uploadDir;
    private String uploadUrlPrefix;
    private String maxUploadSize;
    private String cloudinaryUrl;
}
