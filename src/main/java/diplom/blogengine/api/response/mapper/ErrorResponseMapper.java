package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.ErrorResponse;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseMapper {

    public ErrorResponse errorResponse(String message) {
        return new ErrorResponse(message);
    }
}
