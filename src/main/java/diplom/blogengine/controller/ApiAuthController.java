package diplom.blogengine.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class ApiAuthController {

    @GetMapping("/api/auth/check")
    @ResponseBody
    public String authCheck() {
        return "auth_check";
    }

}
