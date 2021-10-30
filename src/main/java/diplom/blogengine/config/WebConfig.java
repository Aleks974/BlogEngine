package diplom.blogengine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import diplom.blogengine.service.ModerationDecision;
import diplom.blogengine.service.converter.*;
import diplom.blogengine.service.util.ModerationDecisionDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;

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
        exposeDirectory(blogSettings.getUploadDir(), registry);
    }

    private void exposeDirectory(String dirName, ResourceHandlerRegistry registry) {
        String absolutePath = Path.of(dirName).toAbsolutePath().toString();
        registry.addResourceHandler(getResourcePattern(dirName))
                .addResourceLocations(getResourceLocation(absolutePath));
    }

    private String getResourcePattern(String dirName) {
        if (!dirName.startsWith("/")) {
            dirName = "/".concat(dirName);
        }
        return  dirName.concat("/**");
    }

    private String getResourceLocation(String absolutePath) {
        if (!absolutePath.endsWith("/")) {
            absolutePath = absolutePath.concat(File.separator);
        }
        return  "file:/".concat(absolutePath);
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
