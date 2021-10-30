package diplom.blogengine.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface IFileStorageService {
    String storeFile(final MultipartFile imageFile, long authUserId);

    String storePhoto(MultipartFile photo, long authUserId);

    void deleteFile(String filename);
}
