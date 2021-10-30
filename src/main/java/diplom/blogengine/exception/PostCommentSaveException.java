package diplom.blogengine.exception;

public class PostCommentSaveException extends RuntimeException {
    public PostCommentSaveException(String msg) {
        super(msg);
    }

    public PostCommentSaveException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
