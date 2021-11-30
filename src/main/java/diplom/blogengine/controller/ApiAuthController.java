package diplom.blogengine.controller;

import diplom.blogengine.api.request.UserNewPasswordRequest;
import diplom.blogengine.api.request.UserRegisterDataRequest;
import diplom.blogengine.api.request.UserLoginRequest;
import diplom.blogengine.api.request.UserResetPasswordRequest;
import diplom.blogengine.api.response.mapper.ErrorResponseMapper;
import diplom.blogengine.security.IAuthenticationService;
import diplom.blogengine.service.ICaptchaService;
import diplom.blogengine.service.IOptionsSettingsService;
import diplom.blogengine.service.IUserService;
import diplom.blogengine.service.util.DdosAtackDefender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
public class ApiAuthController {
    private final ICaptchaService captchaService;
    private final IUserService userService;
    private final IAuthenticationService authService;
    private final DdosAtackDefender ddosAtackDefender;
    private final ErrorResponseMapper errorResponseMapper;
    private final IOptionsSettingsService optionsSettingsService;

    public ApiAuthController(ICaptchaService captchaService,
                             IUserService userService,
                             IAuthenticationService authService,
                             IOptionsSettingsService optionsSettingsService,
                             DdosAtackDefender ddosAtackDefender,
                             ErrorResponseMapper errorResponseMapper) {
        this.captchaService = captchaService;
        this.userService = userService;
        this.authService = authService;
        this.optionsSettingsService = optionsSettingsService;
        this.ddosAtackDefender = ddosAtackDefender;
        this.errorResponseMapper = errorResponseMapper;
    }

    @GetMapping("/api/auth/check")
    public ResponseEntity<?> authCheck() {
        log.debug("enter authCheck()");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authService.getAuthData());
    }

    @GetMapping("/api/auth/captcha")
    public ResponseEntity<?> getCaptcha(HttpServletRequest request) {
        log.debug("enter getCaptcha()");

        if (!ddosAtackDefender.validateCaptchaRequest(request)) {
            return errorResponseMapper.toManyRequests();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(captchaService.generateCaptchaDataAndDeleteOld());
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegisterDataRequest userRegisterDataRequest,
                                          HttpServletRequest request) {
        log.debug("enter registerUser()");

        if (authService.isAuthenticated()) {
            return ResponseEntity.notFound().build();
        }

        if (!optionsSettingsService.multiUserModeIsEnabled()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.registerUser(userRegisterDataRequest, request.getLocale()));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> loginUser(HttpServletRequest httpRequest, @RequestBody @Valid UserLoginRequest userLoginRequest) {
        log.debug("enter loginUser()");

        authService.loginUser(httpRequest, userLoginRequest);
        log.debug("success login user, id: " + authService.getAuthenticatedUserId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authService.getAuthData());
    }

    @GetMapping("/api/auth/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        log.debug("enter logoutUser()");

        authService.logoutUser(httpRequest, httpResponse);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authService.getLogoutData());
    }

    @PostMapping("/api/auth/restore")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid UserResetPasswordRequest resetPasswordRequest,
                                             HttpServletRequest request) {
        log.debug("enter resetPassword()");

        if (authService.isAuthenticated()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.resetPassword(resetPasswordRequest, request.getLocale()));
    }

    @PostMapping("/api/auth/password")
    public ResponseEntity<?> saveNewPassword(@RequestBody @Valid UserNewPasswordRequest newPasswordRequest,
                                                  HttpServletRequest request) {
        log.debug("enter saveNewPassword()");

        if (authService.isAuthenticated()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.saveNewPassword(newPasswordRequest, request.getLocale()));
    }


}
