package diplom.blogengine.exception;

public class FileStorageException extends RuntimeException {
    private boolean isMessageSourceKey;

    public FileStorageException(String msg) {
        super(msg);
    }

    public FileStorageException(String msg, boolean isMessageSourceKey) {
        super(msg);
        this.isMessageSourceKey = isMessageSourceKey;
    }

    public FileStorageException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public FileStorageException(String msg, Throwable cause, boolean isMessageSourceKey) {
        super(msg, cause);
        this.isMessageSourceKey = isMessageSourceKey;
    }

    public boolean isMessageSourceKey() {
        return isMessageSourceKey;
    }

}
