package diplom.blogengine.exception;

public class SendEmailFailedException extends RuntimeException {
    public SendEmailFailedException(String msg) {
        super(msg);
    }

    public SendEmailFailedException(String msg, Throwable cause) {
        super(msg + System.lineSeparator() + " nested exception: " + cause.getMessage(), cause);
    }

}
