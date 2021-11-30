package diplom.blogengine.exception;

public class UserNotFoundException extends RuntimeException {
    private static final String message = "error.user.notFound";
    private final long userId;
    private final String email;

    public UserNotFoundException(long userId) {
        this.userId = userId;
        this.email = "";
    }

    public UserNotFoundException(String email) {
        this.email = email;
        this.userId = 0;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public long getUserId() {
        return userId;
    }
}
