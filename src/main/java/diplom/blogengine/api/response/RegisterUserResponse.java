package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterUserResponse {
    @NonNull
    private final boolean result;
    private final Map<String, String> errors;

    public RegisterUserResponse(boolean result) {
        this(result, null);
    }

    public RegisterUserResponse(boolean result, Map<String, String> errors) {
        this.result = result;
        this.errors = errors;
    }
}
