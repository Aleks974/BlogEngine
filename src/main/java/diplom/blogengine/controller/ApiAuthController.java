package diplom.blogengine.controller;

import diplom.blogengine.api.request.UserDataRequest;
import diplom.blogengine.api.response.AuthCheckResponse;
import diplom.blogengine.api.response.RegisterUserResponse;
import diplom.blogengine.api.response.mapper.ErrorResponseMapper;
import diplom.blogengine.service.ICaptchaService;
import diplom.blogengine.service.IUserService;
import diplom.blogengine.service.util.DdosAtackDefender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
public class ApiAuthController {
    private final ICaptchaService captchaService;
    private final IUserService userService;
    private final DdosAtackDefender ddosAtackDefender;
    private final ErrorResponseMapper errorResponseMapper;

    public ApiAuthController(ICaptchaService captchaService,
                             IUserService userService,
                             DdosAtackDefender ddosAtackDefender,
                             ErrorResponseMapper errorResponseMapper) {
        this.captchaService = captchaService;
        this.userService = userService;
        this.ddosAtackDefender = ddosAtackDefender;
        this.errorResponseMapper = errorResponseMapper;
    }

    @GetMapping("/api/auth/check")
    public ResponseEntity<?> authCheck() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthCheckResponse(false, null));
    }

    @GetMapping("/api/auth/captcha")
    public ResponseEntity<?> getCaptcha(HttpServletRequest request) {
        if (!ddosAtackDefender.validateCaptchaRequest(request)) {
            return errorResponseMapper.toManyRequests();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(captchaService.generateCaptchaDataAndDeleteOld());
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserDataRequest userData) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.registerUser(userData));
    }
}
