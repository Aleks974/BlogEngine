package diplom.blogengine.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.exception.FileStorageException;
import diplom.blogengine.service.util.ImageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class CloudinaryFileStorageService extends AbstractFileStorage {
    private final Cloudinary cloudinary;
    private final String URL_KEY = "url";
    private final String SECURE_URL_KEY = "secure_url";
    private final ImageHelper imageHelper;

    public CloudinaryFileStorageService(BlogSettings blogSettings, ImageHelper imageHelper) {
        super(blogSettings);
        this.imageHelper = imageHelper;
        cloudinary = new Cloudinary(Objects.requireNonNull(blogSettings.getCloudinaryUrl(), "cloudinaryUrl is null"));
    }

    @Override
    public String storeFile(MultipartFile multipartFile, long authUserId) {
        log.debug("enter storeFile()");

        Objects.requireNonNull(multipartFile, "file is null");
        String filename = validateMultipartFile(multipartFile);
        validateExtension(filename, allowedExtensions);

        try {
            byte[] fileBytes = multipartFile.getBytes();
            return uploadToCloud(fileBytes);
        } catch (IOException ex) {
            throw new FileStorageException("error.file.cloudStoreFailure", ex, true);
        }
    }

    @Override
    public String storeImage(MultipartFile multipartFile, long authUserId, int resizeWidth, int resizeHeight) {
        log.debug("enter storeImage()");

        Objects.requireNonNull(multipartFile, "file is null");
        String filename = validateMultipartFile(multipartFile);
        String fileExt = validateExtension(filename, allowedExtensions);

        try {
            byte[] imageBytes = multipartFile.getBytes();
            if (resizeWidth > 0 && resizeHeight > 0) {
                imageBytes = imageHelper.resizeImage(imageBytes, fileExt, resizeWidth, resizeHeight);
            }
            return uploadToCloud(imageBytes);
        } catch (IOException ex) {
            throw new FileStorageException("error.file.cloudStoreFailure", ex, true);
        }
    }

    private String uploadToCloud(byte[] fileBytes) throws IOException {
        log.debug("enter uploadToCloud()");

        Map<String, Object> uploadResult = cloudinary.uploader().upload(fileBytes, ObjectUtils.emptyMap());
        if (uploadResult == null) {
            throw new FileStorageException("error.file.cloudStoreFailure", true);
        }
        String resourceUri = (String) uploadResult.getOrDefault(SECURE_URL_KEY, uploadResult.get(URL_KEY));
        if (resourceUri == null || resourceUri.isBlank()) {
            log.warn("resourceUri is null or blank");
            throw new FileStorageException("error.file.cloudStoreFailure", true);
        }
        log.debug("file stored to cloud {}", resourceUri);
        return resourceUri;
    }

    @Override
    public void deleteFile(String filepath) {
        Objects.requireNonNull(filepath, "filename is null");
        deleteFromCloud(filepath);
    }

    private void deleteFromCloud(String uri) {
        log.debug("enter deleteFromCloud()");

        if (!uri.startsWith("http")) {
            throw new IllegalArgumentException("Error while try to delete file: " + uri + ". Perhaps security issue or file is local stored");
        }

        String publicId = getFilenameFromUri(uri);
        if (fileExistsAtCloud(publicId)) {
            try {
                Map<String, Object> deleteResponse = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                if (deleteResponse == null) {
                    log.warn("deleteResult is null: {}");
                    throw new FileStorageException("error.file.cloudDeleteFailure", true);
                }
                String deleteResult = (String) deleteResponse.get("result");
                final String SUCCESS_RESULT = "ok";
                if (deleteResult == null || !deleteResult.equalsIgnoreCase(SUCCESS_RESULT)) {
                    log.warn("deleteResult is not ok: {}", deleteResult);
                    throw new FileStorageException("error.file.cloudDeleteFailure", true);
                }
                log.debug("file deleted from cloud");
            }  catch (IOException ex) {
                throw new FileStorageException("error.file.cloudDeleteFailure", ex, true);
            }
        } else {
            log.debug("file does not exist at cloud: {}", uri);
        }

    }

    private String getFilenameFromUri(String filepath) {
        final int NOT_FOUND = -1;
        int pointInd = filepath.lastIndexOf('.');
        int slashInd = filepath.lastIndexOf('/');
        if (pointInd == NOT_FOUND || slashInd == NOT_FOUND || slashInd >= filepath.length() - 1) {
            log.debug("filename path to delete is not correct: {}", filepath);
            throw new FileStorageException("error.file.cloudDeleteFailure", true);
        }
        return filepath.substring(slashInd + 1, pointInd);
    }

    private boolean fileExistsAtCloud(String publicId) {
        log.debug("enter fileExistsAtCloud()");
        log.debug("publicId: {}", publicId);
        try {
            Map<String, Object> apiResult = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            String fileUrl = (String) apiResult.getOrDefault(SECURE_URL_KEY, apiResult.get(URL_KEY));
            return fileUrl != null && !fileUrl.isBlank();
        } catch (Exception ex) {
            throw new FileStorageException("error.file.cloudSearchFailure", ex, true);
        }
    }

}
