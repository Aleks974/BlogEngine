package diplom.blogengine.service;

import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.exception.FileStorageException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public abstract class AbstractFileStorage implements IFileStorageService {
    protected final Set<String> allowedExtensions;

    public AbstractFileStorage(BlogSettings blogSettings) {
        allowedExtensions = Set.of(blogSettings.getUploadFilesExtensions().split("\\|"));
    }

    protected String validateMultipartFile(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (file.isEmpty() || filename.isBlank()) {
            throw new FileStorageException("error.file.empty", true);
        }
        if (filename.contains("..")) {
            // security check
            throw new FileStorageException("error.file.relativePath", true);
        }
        return filename;
    }

    protected String validateExtension(String filename, Set<String> allowedExtensions) {
        String ext = getExtension(filename);
        if (ext.isEmpty() || !allowedExtensions.contains(ext)) {
            throw new FileStorageException("error.file.prohibitedExt", true);
        }
        return ext;
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
