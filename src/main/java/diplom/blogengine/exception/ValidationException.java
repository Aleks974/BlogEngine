package diplom.blogengine.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {
    private final Map<String, String> errors = new HashMap<>();

    public ValidationException(Map<String, String> errors) {
        this.errors.putAll(errors);
    }

    public Map<String ,String> getErrors() {
        return errors;
    }

    public String getMessage() {
        return errors.toString();
    }
}
