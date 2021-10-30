package diplom.blogengine.controller;

import diplom.blogengine.api.request.GlobalSettingsRequest;
import diplom.blogengine.api.request.UserProfileDataRequest;
import diplom.blogengine.security.IAuthenticationService;
import diplom.blogengine.service.IFileStorageService;
import diplom.blogengine.service.IGeneralService;
import diplom.blogengine.service.IOptionsSettingsService;
import diplom.blogengine.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
public class ApiGeneralController {
    private final IOptionsSettingsService optionsSettingsService;
    private final IGeneralService generalService;
    private final IAuthenticationService authService;
    private final IFileStorageService fileService;
    private final IUserService userService;

    public ApiGeneralController(IGeneralService generalService,
                                IOptionsSettingsService optionsSettingsService,
                                IAuthenticationService authService,
                                IFileStorageService fileService,
                                IUserService userService) {
        this.optionsSettingsService = optionsSettingsService;
        this.generalService = generalService;
        this.authService = authService;
        this.fileService = fileService;
        this.userService = userService;
    }

    @GetMapping("/api/init")
    public ResponseEntity<?> getInitOptions() {
        log.debug("enter getInitOptions()");

        return ResponseEntity.ok(optionsSettingsService.getInitOptions());
    }

    @GetMapping("/api/settings")
    public ResponseEntity<?> getSettings() {
        log.debug("enter getSettings()");

        return ResponseEntity.ok(optionsSettingsService.getGlobalSettings());
    }

    @PutMapping("/api/settings")
    public ResponseEntity<?> updateSettings(@RequestBody @Valid GlobalSettingsRequest globalSettingsRequest,
                                            HttpServletRequest request) {
        log.debug("enter updateSettings()");

        if (!authService.isAuthenticated() || !authService.getAuthenticatedUser().isModerator()) {
            return unauthorizedResponse();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(optionsSettingsService.updateSettings(globalSettingsRequest));
    }

    @GetMapping("/api/statistics/my")
    public ResponseEntity<?> getMyStatisics() {
        log.debug("enter getMyStatisics()");

        if (!authService.isAuthenticated()) {
            return unauthorizedResponse();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(generalService.getMyStatistics(authService.getAuthenticatedUserId()));
    }

    @GetMapping("/api/statistics/all")
    public ResponseEntity<?> getAllStatistics() {
        log.debug("enter getAllStatistics()");

        if (!optionsSettingsService.statisticsIsPublic() &&
                    (!authService.isAuthenticated() || !authService.getAuthenticatedUser().isModerator()) )
        {
            return unauthorizedResponse();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(generalService.getAllStatistics());
    }

    @PostMapping(value = "/api/profile/my", consumes = "application/json")
    public ResponseEntity<?> saveProfile(@RequestBody UserProfileDataRequest userData, HttpServletRequest request) {
        log.debug("enter saveProfile()");

        if (!authService.isAuthenticated()) {
            return unauthorizedResponse();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.saveProfile(userData, authService.getAuthenticatedUser(), request.getLocale()));
    }

    @PostMapping(value = "/api/profile/my", consumes = "multipart/form-data")
    public ResponseEntity<?> saveProfileWithPhoto(@ModelAttribute @NotNull UserProfileDataRequest userData,
                                         @RequestParam("photo") @NotNull MultipartFile photoFile,
                                         HttpServletRequest request) {

        log.debug("enter saveProfileWithPhoto()");

        if (!authService.isAuthenticated()) {
                return unauthorizedResponse();
        }

        userData.setPhotoFile(photoFile);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.saveProfile(userData, authService.getAuthenticatedUser(), request.getLocale()));
    }


    private ResponseEntity<?> unauthorizedResponse() {
        log.debug("enter unauthorizedResponse()");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
