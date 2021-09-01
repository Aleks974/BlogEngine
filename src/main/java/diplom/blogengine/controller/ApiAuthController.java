package diplom.blogengine.controller;

import diplom.blogengine.api.response.AuthCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ApiAuthController {

    @GetMapping("/api/auth/check")
    public AuthCheckResponse authCheck() {
        return new AuthCheckResponse(false, null);
    }

}
