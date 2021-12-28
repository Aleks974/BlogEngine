package diplom.blogengine.service.util;

import diplom.blogengine.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Objects;

@Slf4j
public class UriHelper {

    public static String uriStringFromPath(Path filePath, Path uploadDirRoot, String uploadUrlPrefix) {
        log.debug("enter uriStringFromPath(): filePath: {}, uploadDirRoot: {}, uploadUrlPrefix: {}", filePath.toString(),
                    uploadDirRoot.toString(), uploadUrlPrefix);
        Objects.requireNonNull(filePath);
        Objects.requireNonNull(uploadDirRoot);
        Objects.requireNonNull(uploadUrlPrefix);

        Path relativePath = uploadDirRoot.relativize(filePath);
        String relativePathStr = relativePath.toString().replaceAll("\\\\", "/");
        if (!relativePathStr.startsWith("/")) {
            relativePathStr = "/".concat(relativePathStr);
        }
        String uri = uploadUrlPrefix.concat(relativePathStr);
        log.debug("uri: {}", uri);
        return uri;
    }

    public static Path localPathFromUri(String localUri, Path uploadDirRoot, String uploadUrlPrefix) {
        log.debug("enter localPathFromUri(): uri: {}, uploadDirRoot: {}, uploadUrlPrefix: {}", localUri,
                uploadDirRoot.toString(), uploadUrlPrefix);
        Objects.requireNonNull(localUri);
        Objects.requireNonNull(uploadDirRoot);
        Objects.requireNonNull(uploadUrlPrefix);

        int charPosAfterUrlPrefixWithSlash = uploadUrlPrefix.length() + 1;
        if (!localUri.startsWith(uploadUrlPrefix) || localUri.length() < charPosAfterUrlPrefixWithSlash ) {
            throw new IllegalArgumentException("Error while try to delete file: " + localUri + ". Perhaps seclocalUrity issue or file is at cloud");
        }

        localUri = localUri.substring(charPosAfterUrlPrefixWithSlash);
        if (localUri.startsWith("/")) {
            throw new IllegalArgumentException("Error while try to delete file: " + localUri);
        }

        Path relFilePath = Path.of(localUri);
        Path filePath = uploadDirRoot.resolve(relFilePath);

        log.debug("filePath: {}", filePath.toString());
        return filePath;
    }

}


