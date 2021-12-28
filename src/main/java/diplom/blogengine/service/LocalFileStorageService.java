package diplom.blogengine.service;

import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.exception.FileStorageException;
import diplom.blogengine.service.util.ImageHelper;
import diplom.blogengine.service.util.UriHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

@Slf4j
public class LocalFileStorageService extends AbstractFileStorage {
    private final Path uploadDirRoot;
    private final String uploadUrlPrefix;
    private final ImageHelper imageHelper;

    public LocalFileStorageService(BlogSettings blogSettings, ImageHelper imageHelper) {
        super(blogSettings);
        this.imageHelper = imageHelper;
        uploadDirRoot = Path.of(Objects.requireNonNull(blogSettings.getUploadDir(), "uploadDir is null"));
        uploadUrlPrefix = Objects.requireNonNull(blogSettings.getUploadUrlPrefix(), "uploadUrlPrefix is null");
        createDirs(uploadDirRoot);

        /*System.out.println(FileSystems.getDefault().getPath("test").toAbsolutePath());
        System.out.println(Path.of("test").toAbsolutePath());
        System.out.println(System.getProperty("user.dir"));*/
    }

    @Override
    public String storeFile(final MultipartFile multipartFile, long authUserId) {
        log.debug("enter storeFile()");

        Path uploadedFile = storeUploadedFile(multipartFile, authUserId);
        return UriHelper.uriStringFromPath(uploadedFile, uploadDirRoot, uploadUrlPrefix);
    }

    private Path storeUploadedFile(MultipartFile multipartFile, long authUserId) {
        log.debug("enter storeUploadedFile()");
        Objects.requireNonNull(multipartFile, "file is null");

        String filename = validateMultipartFile(multipartFile);
        validateExtension(filename, allowedExtensions);

        Path uploadFilePath = generateUploadFilePath(filename, authUserId);
        try {
            store(multipartFile.getBytes(), uploadFilePath);
        } catch (IOException ex) {
          throw new FileStorageException("error.file.ioFailure", ex, true);
        }
        return uploadFilePath;
    }

    @Override
    public String storeImage(final MultipartFile multipartFile, long authUserId, int resizeWidth, int resizeHeight) {
        log.debug("enter storeImage()");

        Path uploadedFile = storeUploadedImage(multipartFile, authUserId, resizeWidth, resizeHeight);
        return UriHelper.uriStringFromPath(uploadedFile, uploadDirRoot, uploadUrlPrefix);
    }

    private Path storeUploadedImage(MultipartFile multipartFile, long authUserId, int resizeWidth, int resizeHeight) {
        log.debug("enter storeUploadedImage()");
        Objects.requireNonNull(multipartFile, "file is null");

        String filename = validateMultipartFile(multipartFile);
        String fileExt = validateExtension(filename, allowedExtensions);

        Path uploadFilePath = generateUploadFilePath(filename, authUserId);
        try {
            byte[] imageBytes = multipartFile.getBytes();
            if (resizeWidth > 0 && resizeHeight > 0) {
                imageBytes = imageHelper.resizeImage(imageBytes, fileExt, resizeWidth, resizeHeight);
            }
            store(imageBytes, uploadFilePath);
        } catch (IOException ex) {
            throw new FileStorageException("error.file.ioFailure", ex, true);
        }
        return uploadFilePath;
    }

    @Override
    public void deleteFile(String uri) {
        log.debug("enter deleteFile() {}", uri);
        Objects.requireNonNull(uri, "uri is null");

        Path filePath = UriHelper.localPathFromUri(uri, uploadDirRoot, uploadUrlPrefix);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("file removed {}", filePath.toString());
            } else {
                log.debug("file not existed {}", filePath.toString());
            }
        } catch (IOException ex) {
            throw new FileStorageException("error.file.ioDeleteFailure", ex, true);
        }
    }

    private void store(byte[] bytes, Path filePath) throws IOException {
        log.debug("enter store()");
        Files.write(filePath, bytes, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("file stored to {}", filePath);
    }

    private Path generateUploadFilePath(String filename, long userId) {
        Path uploadDirPath = createUploadFileDirPath(userId);
        return uploadDirPath.resolve(filename);
    }

    private Path createUploadFileDirPath(long userId) {
        StringJoiner relPath = new StringJoiner(File.separator);
        String userDir = generateUserUploadDir(userId);
        relPath.add(userDir);

        LocalDate lc = LocalDate.now();
        int y = lc.getYear();
        int m = lc.getMonthValue();
        int d = lc.getDayOfMonth();
        relPath.add(String.valueOf(y));
        relPath.add(String.valueOf(m));
        relPath.add(String.valueOf(d));

        Path uploadDirPath = uploadDirRoot.resolve(Path.of(relPath.toString()));
        createDirs(uploadDirPath);

        return uploadDirPath;
    }

    private void createDirs(Path path) {
        try {
            if (!Files.exists(path)) {
                Path created;
                if ((created = Files.createDirectories(path)) != null) {
                    log.debug("createDirs(): success creating {}", created.toAbsolutePath().toString());
                } else {
                    log.debug("createDirs(): fail creating {}", path.toAbsolutePath().toString());
                }
            }
        } catch (IOException ex) {
            throw new FileStorageException("error.file.ioFailure", ex, true);
        }
    }

    private String generateUserUploadDir(long userId) {
        return generateHash("user_folder_" + userId) + userId;
    }

    private String generateHash(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes());
    }

}
