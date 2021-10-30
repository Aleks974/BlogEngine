package diplom.blogengine.service;

import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.exception.FileStorageException;
import diplom.blogengine.service.util.ImageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

@Slf4j
@Service
public class FileStorageService implements IFileStorageService {
    private final Path uploadDirRoot;
    private final ImageHelper imageHelper;
    private final int PHOTO_WIDTH = 36;
    private final int PHOTO_HEIGHT = 36;

    private final Set<String> allowedExt = Set.of("JPG", "JPEG", "PNG");

    public FileStorageService(BlogSettings blogSettings, ImageHelper imageHelper) {
        uploadDirRoot = Paths.get(blogSettings.getUploadDir());
        this.imageHelper = imageHelper;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDirRoot);
        } catch (IOException ex) {
            throw new FileStorageException("error.file.ioInitFailure", ex);
        }
    }

    @Override
    public String storeFile(final MultipartFile file, long authUserId) {
        log.debug("enter storeImage()");

        Objects.requireNonNull(file, "file is null");

        String filename = validateAndGetName(file);
        validateExtAndGet(filename);

        Path uploadFilePath = generateUploadFilePath(filename, authUserId);
        store(file, uploadFilePath);

        return createURIstr(uploadFilePath);
    }

    @Override
    public String storePhoto(final MultipartFile file, long authUserId) {
        log.debug("enter storePhoto()");

        Objects.requireNonNull(file, "file is null");

        String filename = validateAndGetName(file);
        String fileExt = validateExtAndGet(filename);

        byte[] photoBytes;
        try(InputStream in = file.getInputStream()) {
            BufferedImage sourceImg = ImageIO.read(in);
            photoBytes = imageHelper.resizeImage(sourceImg, fileExt, PHOTO_WIDTH, PHOTO_HEIGHT);
        } catch (IOException ex) {
            throw new FileStorageException("error.file.ioFailure", ex);
        }

        Path uploadFilePath = generateUploadPhotoPath(filename, authUserId);
        store(photoBytes, uploadFilePath);

        return createURIstr(uploadFilePath);
    }

    @Override
    public void deleteFile(String filename) {
        log.debug("enter deleteFile() {}", filename);

        if (filename.startsWith("/")) {
            final int SECOND_CHAR_POS = 1;
            filename = filename.substring(SECOND_CHAR_POS);
        }
        Path filePath = Path.of(filename);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("file removed {}", filename);
            }
        } catch (IOException ex) {
            throw new FileStorageException("error.file.ioFailure", ex);
        }
    }

    private void store(MultipartFile file, Path filePath) {
        log.debug("enter store()");

        try(InputStream in = file.getInputStream()) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);

            log.debug("file stored to {}", filePath);
        } catch (IOException ex) {
            throw new FileStorageException("error.file.ioFailure", ex);
        }
    }

    private void store(byte[] bytes, Path filePath) {
        log.debug("enter store()");

        try {
            Files.write(filePath, bytes, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            log.debug("file stored to {}", filePath);
        } catch (IOException ex) {
            throw new FileStorageException("error.file.ioFailure", ex);
        }
    }

    private String validateAndGetName(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (file.isEmpty()) {
            throw new FileStorageException("error.file.empty");
        }
        if (filename.contains("..")) {
            // security check
            throw new FileStorageException("error.file.relativePath");
        }
        return filename;
    }

    private String validateExtAndGet(String filename) {
        String ext = getExtension(filename);
        if (ext.isEmpty() || !allowedExt.contains(ext)) {
            throw new FileStorageException("error.file.prohibitedExt");
        }
        return ext;
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

    private Path generateUploadPhotoPath(String filename, long userId) {
        Path uploadDirPath = createUploadPhotoPath(userId);
        return uploadDirPath.resolve(filename);
    }

    private Path createUploadPhotoPath(long userId) {
        String userDir = generateUserUploadDir(userId);
        Path uploadDirPath = uploadDirRoot.resolve(userDir).resolve("photos");
        createDirs(uploadDirPath);

        return uploadDirPath;
    }

    private void createDirs(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException ex) {
            throw new FileStorageException("error.file.ioFailure", ex);
        }
    }

    private String generateUserUploadDir(long userId) {
        return generateHash("user_folder_" + userId) + userId;
    }

    private String generateHash(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes());
    }

    private String createURIstr(Path relFilePath) {
        String uriStr = relFilePath.toString().replace('\\', '/');
        if (!uriStr.startsWith("/")) {
            uriStr = "/".concat(uriStr);
        }
        return uriStr;
    }



    private String getExtension(String filename) {
        int extDotPos = filename.lastIndexOf('.');
        if (extDotPos > 0 && extDotPos < filename.length() - 1) {
            String ext = filename.substring(extDotPos + 1).toUpperCase();
            return ext;
        } else {
            return "";
        }
    }


}
