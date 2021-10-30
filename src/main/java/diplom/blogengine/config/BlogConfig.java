package diplom.blogengine.config;

import diplom.blogengine.service.util.*;
import diplom.blogengine.service.cache.GlobalSettingsCacheHandler;
import diplom.blogengine.service.cache.TagsCacheHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.TimeZone;

@Configuration
public class BlogConfig {
    
    @Bean
    public BlogSettings blogSettings(BlogProperties blogProp) {
        TimeZone timeZone = TimeZone.getTimeZone(Objects.requireNonNull(blogProp.getServerTimeZone()));
        int timeout = blogProp.getCaptchaDeleteTimeout();
        if (timeout <= 0) {
            throw new IllegalArgumentException("Config parameter captchaDeleteTimeout <= 0");
        }
        String uploadDir = Objects.requireNonNull(blogProp.getUploadDir());
        if  (uploadDir.contains("..")) {
            throw new IllegalArgumentException("Config parameter uploadDir contains '..'");
        }
        return BlogSettings.builder()
                .title(Objects.requireNonNull(blogProp.getTitle()))
                .subtitle(Objects.requireNonNull(blogProp.getSubtitle()))
                .phone(Objects.requireNonNull(blogProp.getPhone()))
                .email(Objects.requireNonNull(blogProp.getEmail()))
                .copyright(Objects.requireNonNull(blogProp.getCopyright()))
                .copyrightFrom(Objects.requireNonNull(blogProp.getCopyrightFrom()))
                .serverTimeZone(timeZone)
                .captchaDeleteTimeout(timeout)
                .prohibitedTags(Objects.requireNonNull(blogProp.getProhibitedTags()))
                .uploadDir(Objects.requireNonNull(blogProp.getUploadDir()))
                .maxUploadSize(Objects.requireNonNull(blogProp.getMaxUploadSize()))
                .build();
    }


    @Bean
    public TimestampHelper timestampHelper(BlogSettings blogSettings) {
        return new TimestampHelper(blogSettings.getServerTimeZone());
    }

    @Bean
    public CaptchaGenerator captchaGenerator() throws Exception {
        return new CaptchaGenerator();
    }

    @Bean
    public ContentHelper contentHelper(BlogSettings blogSettings) throws Exception {
        return new ContentHelper(blogSettings.getProhibitedTags());
    }

    @Bean
    public TagsCacheHandler tagsCacheHandler() throws Exception {
        return new TagsCacheHandler();
    }

    @Bean
    public DdosAtackDefender ddosAtackDefender() throws Exception {
        return new DdosAtackDefender();
    }


    @Bean
    public ImageHelper imageHelper() throws Exception {
        return new ImageHelper();
    }

}
