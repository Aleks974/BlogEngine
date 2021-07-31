package diplom.blogengine.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class ApiPostController {

    @GetMapping("/api/post")
    @ResponseBody
    public String post() {
        return "post1";
    }

}
