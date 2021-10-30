package diplom.blogengine.exception;

public class UserNotFoundException extends RuntimeException {
    private static final String message = "error.user.notFound";
    private final long userId;

    public UserNotFoundException(long userId) {
        this.userId = userId;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public long getUserId() {
        return userId;
    }
}
