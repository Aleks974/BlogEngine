package diplom.blogengine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DefaultController {
    private static Logger logger = LoggerFactory.getLogger(DefaultController.class);

    @GetMapping("/")
    public String index() {
        logger.debug("Index requested");
        return "index";
    }
}
