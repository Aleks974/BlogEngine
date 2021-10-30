package diplom.blogengine.controller;

import diplom.blogengine.security.IAuthenticationService;
import diplom.blogengine.service.IFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Slf4j
@RestController
public class ApiImageController {
    private final IAuthenticationService authService;
    private final IFileStorageService fileStorageService;

    public ApiImageController(IAuthenticationService authService, IFileStorageService fileStorageService) {
        this.authService = authService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/api/image", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(@RequestParam("image") @NotNull MultipartFile imageFile) {
        log.debug("enter uploadImage()");

        if (!authService.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        //String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/upload/").path(imageFile.getName()).toUriString();
        String uri = fileStorageService.storeFile(imageFile, authService.getAuthenticatedUserId());

        return ResponseEntity.ok(uri);
    }

}
