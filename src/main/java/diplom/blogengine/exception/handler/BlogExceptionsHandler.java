package diplom.blogengine.exception.handler;

import diplom.blogengine.api.response.mapper.ErrorResponseMapper;
import diplom.blogengine.exception.PostNotFoundException;
import diplom.blogengine.exception.RequestParamDateParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@Slf4j
@ControllerAdvice
public class BlogExceptionsHandler extends ResponseEntityExceptionHandler {

    private final ErrorResponseMapper errorResponseMapper;

    public BlogExceptionsHandler(ErrorResponseMapper errorResponseMapper) {
        this.errorResponseMapper = errorResponseMapper;
    }

    // request parameter's type conversion fails
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.debug("enter to handleMethodArgumentTypeMismatchException()");

        String message = String.format("Request parameter '%s' has invalid type", ex.getName());
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(message));
    }


    // validation of request parameters fails
    @ExceptionHandler({ConstraintViolationException.class})
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        log.debug("enter to handleConstraintViolationException()");

        StringBuilder messageBuilder = new StringBuilder();
        for (ConstraintViolation violation : ex.getConstraintViolations()) {
            if (messageBuilder.length() != 0) {
                messageBuilder.append(", ");
            }
            messageBuilder.append(String.format("Request parameter '%s' has invalid value: %s", violation.getPropertyPath(), violation.getMessage()));
        }
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(messageBuilder.toString()));
    }


    // validation of request bean's fields fails
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.debug("enter to handleMethodArgumentNotValid()");

        StringBuilder builder = new StringBuilder();
        for (FieldError err : ex.getFieldErrors()) {
            if (builder.length() != 0) {
                builder.append(", ");
            }
            builder.append("Request field ");
            builder.append(err.getField());
            builder.append(" ");
            builder.append(err.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(builder.toString()));
    }


    @ExceptionHandler({ RequestParamDateParseException.class })
    protected ResponseEntity<Object> handleRequestParamDateParseException(RequestParamDateParseException ex, WebRequest request) {
        log.debug("enter to handleRequestParamDateParseException()");

        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(ex.getMessage()));
    }


    // missed required request parameter
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(org.springframework.web.bind.MissingServletRequestParameterException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatus status, org.springframework.web.context.request.WebRequest request) {
        log.debug("enter to handleMissingServletRequestParameter()");

        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(ex.getMessage()));
    }


    // post not found
    @ExceptionHandler({ PostNotFoundException.class })
    protected ResponseEntity<Object> handlePostNotFoundException(PostNotFoundException ex, WebRequest request) {
        log.debug("enter to handlePostNotFoundException()");

        return ResponseEntity.notFound().build();
    }


    // method not allowed 405
    //ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, headers, .HttpStatus status, WebRequest request) {
    // }

    // Required request body is missing: - ожидается что будет передан объект
    //protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,HttpStatus status, WebRequest request)

    //Completed 415 UNSUPPORTED_MEDIA_TYPE
    //HttpMediaTypeNotSupportedException

}
