package diplom.blogengine.config;

import diplom.blogengine.repository.*;
import diplom.blogengine.service.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.TimeZone;

@Slf4j
@Configuration
public class BlogConfig {
    
    @Bean
    public BlogSettings blogSettings(BlogProperties blogProp) {
        TimeZone timeZone = TimeZone.getTimeZone(Objects.requireNonNull(blogProp.getServerTimeZone()));
        setDefaultServerTimeZone(timeZone);

        int captchaDeleteTimeout = blogProp.getCaptchaDeleteTimeout();
        if (captchaDeleteTimeout <= 0) {
            throw new IllegalArgumentException("Config parameter captchaDeleteTimeout <= 0");
        }

        String uploadDir = Objects.requireNonNull(blogProp.getUploadDir());
        if  (uploadDir.contains("..")) {
            throw new IllegalArgumentException("Config parameter uploadDir contains '..'");
        }
        log.debug("app url: {}", blogProp.getSiteUrl());

        return BlogSettings.builder()
                .title(Objects.requireNonNull(blogProp.getTitle()))
                .siteUrl(Objects.requireNonNull(blogProp.getSiteUrl()))
                .subtitle(Objects.requireNonNull(blogProp.getSubtitle()))
                .phone(Objects.requireNonNull(blogProp.getPhone()))
                .email(Objects.requireNonNull(blogProp.getEmail()))
                .copyright(Objects.requireNonNull(blogProp.getCopyright()))
                .copyrightFrom(Objects.requireNonNull(blogProp.getCopyrightFrom()))
                .serverTimeZone(timeZone)
                .captchaDeleteTimeout(captchaDeleteTimeout)
                .permittedTags(Objects.requireNonNull(blogProp.getPermittedTags()))
                .uploadDir(Objects.requireNonNull(blogProp.getUploadDir()))
                .maxUploadSize(Objects.requireNonNull(blogProp.getMaxUploadSize()))
                .build();
    }

    private void setDefaultServerTimeZone(TimeZone timeZoneFromSettings) {
        TimeZone serverDefaultTimeZone = TimeZone.getDefault();
        log.debug("time with default timeZone: {} at {}", LocalDateTime.now(), serverDefaultTimeZone.toZoneId());

        TimeZone.setDefault(timeZoneFromSettings);
        log.debug("time after set timeZone from settings: {} at {}", LocalDateTime.now(), timeZoneFromSettings.toZoneId());
//        for (String s: TimeZone.getAvailableIDs()) {
//            System.out.println(s);
//        }
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
        return new ContentHelper(blogSettings.getPermittedTags());
    }


    @Bean
    public DdosAtackDefender ddosAtackDefender() throws Exception {
        return new DdosAtackDefender();
    }


    @Bean
    public ImageHelper imageHelper() throws Exception {
        return new ImageHelper();
    }

    @Bean
    public MailHelper mailHelper(BlogSettings blogSettings, MailSender mailSender, MessageSource messageSource) throws Exception {
        return new MailHelper(blogSettings, mailSender, messageSource);
    }

    // caches for repositories
    @Bean
    public CachedTagRepository cachedTagRepository(TagRepository tagRepository) {
        return new CachedTagRepository(tagRepository);
    }

    @Bean
    public CachedPostRepository cachedPostRepository(PostRepository postRepository) {
        return new CachedPostRepository(postRepository);
    }

    @Bean
    public CachedSettingsRepository cachedSettingsRepository(SettingsRepository settingsRepository) {
        return new CachedSettingsRepository(settingsRepository);
    }

    @Bean
    public PostsCounterStorage postsCounterStorage() {
        return new PostsCounterStorage();
    }
}
