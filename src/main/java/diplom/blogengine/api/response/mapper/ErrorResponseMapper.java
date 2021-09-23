package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseMapper {

    public ErrorResponse errorResponse(String message) {
        return new ErrorResponse(message);
    }

    public ResponseEntity<String> toManyRequests() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("To many request from this IP");
    }

}
