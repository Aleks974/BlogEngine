package diplom.blogengine.config;

import diplom.blogengine.service.converter.MyPostStatusConverter;
import diplom.blogengine.service.converter.PostSortModeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new PostSortModeConverter());
        registry.addConverter(new MyPostStatusConverter());
    }

   /* @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login_page.html").setViewName("login_page");
        registry.addViewController("/user.html").setViewName("user_page");
    }*/
}
