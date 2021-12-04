package diplom.blogengine.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class DefaultController {
    @GetMapping({"/", "/posts/**", "/post/**", "/tag/**", "/calendar/**", "/stat/**", "/search/**", "/login/**", "/logout/**",
            "settings/**", "/profile/**", "/add/**", "/my/**", "/moderation/**"})
    public String index() {
        return "index";
    }
}
