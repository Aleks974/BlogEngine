package diplom.blogengine.config;

import lombok.Builder;
import lombok.Value;

import java.util.TimeZone;

@Value
@Builder
public class BlogSettings {
    private String title;
    private String subtitle;
    private String phone;
    private String email;
    private String copyright;
    private String copyrightFrom;
    private TimeZone serverTimeZone;
    private String hashAlgorithm;
    private int captchaDeleteTimeout;
}
