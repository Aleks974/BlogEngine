package diplom.blogengine.exception.handler;

import diplom.blogengine.api.response.mapper.AuthResponsesMapper;
import diplom.blogengine.api.response.mapper.ErrorResponseMapper;
import diplom.blogengine.exception.PostNotFoundException;
import diplom.blogengine.exception.RequestParamDateParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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
    private final AuthResponsesMapper authResponsesMapper;

    public BlogExceptionsHandler(ErrorResponseMapper errorResponseMapper, AuthResponsesMapper authResponsesMapper) {
        this.errorResponseMapper = errorResponseMapper;
        this.authResponsesMapper = authResponsesMapper;
    }
    // ToDo javadocs
    // request parameter's type conversion fails
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.debug("enter to handleMethodArgumentTypeMismatchException()");

        String msg = String.format("Request parameter '%s' has invalid type", ex.getName());
        log.debug(msg);
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(msg));
    }


    // validation of request parameters fails
    @ExceptionHandler({ConstraintViolationException.class})
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        log.debug("enter to handleConstraintViolationException()");

        StringBuilder msg = new StringBuilder();
        for (ConstraintViolation violation : ex.getConstraintViolations()) {
            if (msg.length() != 0) {
                msg.append(", ");
            }
            msg.append(String.format("Request parameter '%s' has invalid value: %s", violation.getPropertyPath(), violation.getMessage()));
        }
        log.debug(msg.toString());
        return ResponseEntity.badRequest()
              .body(errorResponseMapper.errorResponse(msg.toString()));
    }


    // validation of request bean's fields fails
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.debug("enter to handleMethodArgumentNotValid()");

        StringBuilder msg = new StringBuilder();
        for (FieldError err : ex.getFieldErrors()) {
            if (msg.length() != 0) {
                msg.append(", ");
            }
            msg.append("Request field ");
            msg.append(err.getField());
            msg.append(" ");
            msg.append(err.getDefaultMessage());
        }

        log.debug(msg.toString());
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(msg.toString()));
    }


    @ExceptionHandler({ RequestParamDateParseException.class })
    protected ResponseEntity<Object> handleRequestParamDateParseException(RequestParamDateParseException ex, WebRequest request) {
        log.debug("enter to handleRequestParamDateParseException()");
        log.debug(ex.getMessage());
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(ex.getMessage()));
    }


    // missed required request parameter
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(org.springframework.web.bind.MissingServletRequestParameterException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatus status, org.springframework.web.context.request.WebRequest request) {
        log.debug("enter to handleMissingServletRequestParameter()");
        log.debug(ex.getMessage());
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(ex.getMessage()));
    }


    // post not found
    @ExceptionHandler({ PostNotFoundException.class })
    protected ResponseEntity<Object> handlePostNotFoundException(PostNotFoundException ex, WebRequest request) {
        log.debug("enter to handlePostNotFoundException()");
        log.debug(ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    // bad password or login, authentication not passed
    @ExceptionHandler({ BadCredentialsException.class })
    protected ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.debug("enter to handleBadCredentialsException()");
        log.debug(ex.getMessage());
        return ResponseEntity.ok().body(authResponsesMapper.failAuthResponse());
    }


    // ToDo log 404 errors, and custom page,

    // method not allowed 405
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.debug("enter to handleHttpRequestMethodNotSupported()");

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // TODo
    // Required request body is missing: - ожидается что будет передан объект, или ошибка в принимаемом json body
    //protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,HttpStatus status, WebRequest request)

    //Completed 415 UNSUPPORTED_MEDIA_TYPE
    //HttpMediaTypeNotSupportedException

}
