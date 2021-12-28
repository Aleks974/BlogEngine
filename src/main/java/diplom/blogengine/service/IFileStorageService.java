package diplom.blogengine.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface IFileStorageService {
    String storeFile(final MultipartFile imageFile, long authUserId);

    String storeImage(MultipartFile photo, long authUserId, int resizeWidth, int resizeHeight);

    void deleteFile(String filename);
}
