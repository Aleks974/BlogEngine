package diplom.blogengine.config;

import diplom.blogengine.repository.*;
import diplom.blogengine.service.util.*;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

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
                .siteUrl(Objects.requireNonNull(blogProp.getSiteUrl()))
                .subtitle(Objects.requireNonNull(blogProp.getSubtitle()))
                .phone(Objects.requireNonNull(blogProp.getPhone()))
                .email(Objects.requireNonNull(blogProp.getEmail()))
                .copyright(Objects.requireNonNull(blogProp.getCopyright()))
                .copyrightFrom(Objects.requireNonNull(blogProp.getCopyrightFrom()))
                .serverTimeZone(timeZone)
                .captchaDeleteTimeout(timeout)
                .permittedTags(Objects.requireNonNull(blogProp.getPermittedTags()))
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
