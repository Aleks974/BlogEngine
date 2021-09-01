package diplom.blogengine.exception.handler;

import diplom.blogengine.api.response.mapper.ErrorResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class BlogExceptionsHandler extends ResponseEntityExceptionHandler {

    private final ErrorResponseMapper errorResponseMapper;

    public BlogExceptionsHandler(ErrorResponseMapper errorResponseMapper) {
        this.errorResponseMapper = errorResponseMapper;
    }

    // request parameter's type conversion fails
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, NumberFormatException.class})
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String message = String.format("Request parameter '%s' has invalid type", ex.getName());
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(message));
    }

    // validation of request parameters and bean's fields fails
    @ExceptionHandler({ConstraintViolationException.class})
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        StringBuilder messageBuilder = new StringBuilder();
        for (ConstraintViolation violation : ex.getConstraintViolations()) {
            if (messageBuilder.length() != 0) {
                messageBuilder.append(", ");
            }
            messageBuilder.append(String.format("Request parameter '%s' has invalid value: %s", violation.getPropertyPath(), violation.getMessage()));
            ;
        }
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(messageBuilder.toString()));
    }


}
