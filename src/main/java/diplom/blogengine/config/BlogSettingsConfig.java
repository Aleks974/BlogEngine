package diplom.blogengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.TimeZone;

@Configuration
public class BlogSettingsConfig {
    
    @Bean
    public BlogSettings blogSettings(BlogProperties blogProp) {
        TimeZone timeZone = TimeZone.getTimeZone(Objects.requireNonNull(blogProp.getServerTimeZone()));
        return BlogSettings.builder()
                .title(Objects.requireNonNull(blogProp.getTitle()))
                .subtitle(Objects.requireNonNull(blogProp.getSubtitle()))
                .phone(Objects.requireNonNull(blogProp.getPhone()))
                .email(Objects.requireNonNull(blogProp.getEmail()))
                .copyright(Objects.requireNonNull(blogProp.getCopyright()))
                .copyrightFrom(Objects.requireNonNull(blogProp.getCopyrightFrom()))
                .serverTimeZone(timeZone)
                .hashAlgorithm(Objects.requireNonNull(blogProp.getHashAlgorithm()))
                .postSortModes(Objects.requireNonNull(blogProp.getPostSortModes()))
                .build();
    }
}
