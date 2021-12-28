package diplom.blogengine.config;

import lombok.Builder;
import lombok.Value;

import java.util.TimeZone;

@Value
@Builder
public class BlogSettings {
    private String title;
    private String siteUrl;
    private String subtitle;
    private String phone;
    private String email;
    private String copyright;
    private String copyrightFrom;
    private TimeZone serverTimeZone;
    private int captchaDeleteTimeout;
    private String permittedTags;
    private String uploadFilesExtensions;
    private String uploadDir;
    private String uploadUrlPrefix;
    private String maxUploadSize;
    private String cloudinaryUrl;
}
