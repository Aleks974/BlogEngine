package diplom.blogengine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import diplom.blogengine.service.ModerationDecision;
import diplom.blogengine.service.converter.*;
import diplom.blogengine.service.util.ModerationDecisionDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final BlogSettings blogSettings;

    public WebConfig(BlogSettings blogSettings) {
        this.blogSettings = blogSettings;
    }

    @Autowired
    public void init(ObjectMapper objectMapper) {
        if (objectMapper != null) {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(ModerationDecision.class, new ModerationDecisionDeserializer());
            objectMapper.registerModule(module);
        }
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new PostSortModeConverter());
        registry.addConverter(new MyPostStatusConverter());
        registry.addConverter(new VoteParameterConverter());
        registry.addConverter(new ModerationStatusConverter());
    }

    @Override
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource());
        return validatorFactoryBean;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = blogSettings.getUploadDir();
        String uploadUrlPrefix = blogSettings.getUploadUrlPrefix();
        if (uploadDir != null && uploadUrlPrefix != null) {
            String uploadResourcePattern = getResourcePattern(uploadUrlPrefix);
            String resourceLocation = getResourceLocation(uploadDir);
            registry.addResourceHandler(uploadResourcePattern).addResourceLocations(resourceLocation);
            log.debug("upload resourcePattern: {}, resourceLocation: {}", uploadResourcePattern, resourceLocation);
        }
    }

    private String getResourcePattern(String urlPrefix) {
        if (!urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix.concat("/");
        }
        return  urlPrefix.concat("**");
    }

    private String getResourceLocation(String dirName) {
        String absolutePath = Path.of(dirName).toAbsolutePath().toString();
        String pathSeparator = File.separator;
        if (!absolutePath.endsWith(pathSeparator)) {
            absolutePath = absolutePath.concat(pathSeparator);
        }
        String prefix;
        if (osIsWindows()) {
            prefix = "file:///";
        } else {
            if (!absolutePath.startsWith(pathSeparator)) {
                absolutePath = pathSeparator.concat(absolutePath);
            }
            prefix = "file:";
        }
        return  prefix.concat(absolutePath);
    }

    private boolean osIsWindows() {
        String os = System.getenv("OS");
        return os != null && os.toLowerCase().contains("windows");
    }


    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> containerCustomizer() {
        return container -> {
            container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND,
                    "/404"));
        };
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // или можно через обычный контроллер с аннотацией @GetMapping("/404")
        registry.addViewController("/404").setViewName("index");
    }

    /*@Bean
    public HttpMessageConverter<Object> createJsonHttpMessageConverter() {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        return messageConverter;
    }  */

   /* @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login_page.html").setViewName("login_page");
        registry.addViewController("/user.html").setViewName("user_page");
    }*/
}
