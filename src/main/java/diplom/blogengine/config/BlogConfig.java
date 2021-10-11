package diplom.blogengine.config;

import diplom.blogengine.service.util.*;
import diplom.blogengine.service.util.cache.GlobalSettingsCacheHandler;
import diplom.blogengine.service.util.cache.TagsCacheHandler;
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
        return BlogSettings.builder()
                .title(Objects.requireNonNull(blogProp.getTitle()))
                .subtitle(Objects.requireNonNull(blogProp.getSubtitle()))
                .phone(Objects.requireNonNull(blogProp.getPhone()))
                .email(Objects.requireNonNull(blogProp.getEmail()))
                .copyright(Objects.requireNonNull(blogProp.getCopyright()))
                .copyrightFrom(Objects.requireNonNull(blogProp.getCopyrightFrom()))
                .serverTimeZone(timeZone)
                .hashAlgorithm(Objects.requireNonNull(blogProp.getHashAlgorithm()))
                .captchaDeleteTimeout(timeout)
                .build();
    }


    @Bean
    public TimestampHelper timestampHelper(BlogSettings blogSettings) {
        return new TimestampHelper(blogSettings.getServerTimeZone());
    }

    @Bean
    public PasswordHelper passwordHelper(BlogSettings blogSettings) throws Exception {
        return new PasswordHelper(blogSettings.getHashAlgorithm());
    }

    @Bean
    public CaptchaGenerator captchaGenerator() throws Exception {
        return new CaptchaGenerator();
    }

    @Bean
    public ContentHelper contentHelper() throws Exception {
        return new ContentHelper();
    }

    @Bean
    public TagsCacheHandler tagsCacheHandler() throws Exception {
        return new TagsCacheHandler();
    }

    @Bean
    public GlobalSettingsCacheHandler globalSettingsCacheHandler() throws Exception {
        return new GlobalSettingsCacheHandler();
    }


    @Bean
    public DdosAtackDefender ddosAtackDefender() throws Exception {
        return new DdosAtackDefender();
    }


}
