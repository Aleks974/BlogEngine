package diplom.blogengine.exception.handler;

import diplom.blogengine.api.response.mapper.AuthResponsesMapper;
import diplom.blogengine.api.response.mapper.ErrorResponseMapper;
import diplom.blogengine.api.response.mapper.ResultResponseMapper;
import diplom.blogengine.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
@ControllerAdvice
public class BlogExceptionsHandler extends ResponseEntityExceptionHandler {

    private final ErrorResponseMapper errorResponseMapper;
    private final AuthResponsesMapper authResponsesMapper;
    private final ResultResponseMapper resultResponseMapper;
    private final MessageSource messageSource;

    public BlogExceptionsHandler(ErrorResponseMapper errorResponseMapper,
                                 AuthResponsesMapper authResponsesMapper,
                                 ResultResponseMapper resultResponseMapper,
                                 MessageSource messageSource) {
        this.errorResponseMapper = errorResponseMapper;
        this.authResponsesMapper = authResponsesMapper;
        this.resultResponseMapper = resultResponseMapper;
        this.messageSource = messageSource;
    }

    // ToDo javadocs
    // request parameter's type conversion fails
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("enter to handleMethodArgumentTypeMismatchException(), message: {}", ex.getMessage());

        // ToDo messageSource
        String msg = String.format("Request parameter '%s' has invalid type", ex.getName());
        log.debug(msg);
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(msg));
    }


    // validation of request parameters fails
    @ExceptionHandler({ConstraintViolationException.class})
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        log.warn("enter to handleConstraintViolationException(), message: {}", ex.getMessage());

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
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn("enter to handleMethodArgumentNotValid(), message: {}", ex.getMessage());

/*        StringJoiner msg = new StringJoiner(", ");
        for (FieldError err : ex.getFieldErrors()) {
            msg.add(err.getDefaultMessage());
        }
        log.debug(msg.toString());
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(msg.toString()));
        StringJoiner msg = new StringJoiner(", ");*/

        Map<String, String> errors = new HashMap<>();
        for (FieldError err : ex.getFieldErrors()) {
            errors.put(err.getField(), err.getDefaultMessage());
        }
        log.debug("handleMethodArgumentNotValid(), errors: {}", errors.toString());
        return ResponseEntity.badRequest()
                .body(resultResponseMapper.failure(errors));
    }


    // missed required request parameter
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(org.springframework.web.bind.MissingServletRequestParameterException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatus status, org.springframework.web.context.request.WebRequest request) {
        log.warn("enter to handleMissingServletRequestParameter(), message: {}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(ex.getMessage()));
    }


    // method not allowed 405
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn("enter to handleHttpRequestMethodNotSupported(), message: {}", ex.getMethod());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // Required request body is missing: - ожидается что будет передан объект, или ошибка при парсинге полей в принимаемом json body, или в структтуре json
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn("enter to handleHttpMessageNotReadable(), message: {}", ex.getMessage());

        Throwable cause = ex.getRootCause();
        if (cause instanceof InputParameterException) {
            String msg =  messageSource.getMessage(cause.getMessage(), null, request.getLocale());
            return ResponseEntity.badRequest().body(errorResponseMapper.errorResponse(msg));
        }
        return ResponseEntity.badRequest().build();
    }

    //Completed 415 UNSUPPORTED_MEDIA_TYPE
    // когда нельзя преобразовать входные параметры в объект?
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn("enter to handleHttpMediaTypeNotSupported(), message: {}", ex.getMessage());

        String msg = messageSource.getMessage("error.HttpMediaTypeNotSupportedException", null, request.getLocale());
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(msg));
    }

    //Failed to convert property value of one type to another while binding fields in object
    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn("enter to handleBindException(), message: {}", ex.getMessage());

        String msg = messageSource.getMessage("error.BindException", null, request.getLocale());
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(msg));
    }

/*    @ExceptionHandler({ NullPointerException.class })
    protected ResponseEntity<Object> handleNullPointerException(NullPointerException ex, WebRequest request) {
        log.debug("enter to handleNullPointerException(), message: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }*/

    @ExceptionHandler({ RequestParamDateParseException.class })
    protected ResponseEntity<Object> handleRequestParamDateParseException(RequestParamDateParseException ex, WebRequest request) {
        log.warn("enter to handleRequestParamDateParseException(), message: {}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(ex.getMessage()));
    }

    // post not found
    @ExceptionHandler({ PostNotFoundException.class })
    protected ResponseEntity<Object> handlePostNotFoundException(PostNotFoundException ex, WebRequest request) {
        log.warn("enter to handlePostNotFoundException(), message: {}", ex.getMessage());

        return ResponseEntity.notFound().build();
    }

    // user not found
    @ExceptionHandler({ UserNotFoundException.class })
    protected ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        log.warn("enter to UserNotFoundException(), message: {}, id: {}", ex.getMessage(), ex.getUserId());

        //String msg = String.format(messageSource.getMessage(ex.getMessage(), null, request.getLocale()), ex.getUserId());

        return ResponseEntity.ok().body(resultResponseMapper.failure());
    }


    // error send email
    @ExceptionHandler({ SendEmailFailedException.class })
    protected ResponseEntity<Object> handleSendEmailFailedException(SendEmailFailedException ex, WebRequest request) {
        log.warn("enter to handleSendEmailFailedException(), message: {}", ex.getMessage());
       /* Throwable cause = ex.getCause();
        if (cause != null) {
            log.warn(cause.getMessage());
        }*/
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // post AccessDenied for edit
    @ExceptionHandler({ PostAccessDeniedException.class })
    protected ResponseEntity<Object> handlePostAccessDeniedException(PostAccessDeniedException ex, WebRequest request) {
        log.warn("enter to handlePostAccessDeniedException(), message: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }


    // bad password or login, authentication not passed
    @ExceptionHandler({ BadCredentialsException.class })
    protected ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.warn("enter to handleBadCredentialsException(), message: {}",  ex.getMessage());

        return ResponseEntity.ok().body(resultResponseMapper.failure());
    }


    @ExceptionHandler({ InputParameterException.class })
    protected ResponseEntity<Object> handleInputParameterException(InputParameterException ex, WebRequest request) {
        log.warn("enter to handleInputParameterException() message: {}, param: {}, value: {}",
                    ex.getMessage(), ex.getParam(), ex.getValue());

        String msg = messageSource.getMessage(ex.getMessage(), null, request.getLocale());
        return ResponseEntity.badRequest()
                .body(errorResponseMapper.errorResponse(msg));
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, WebRequest request) {
        log.warn("enter to handleMaxUploadSizeExceededException(), message: {}", ex.getMessage());

        String msg = messageSource.getMessage("error.file.maxUploadSizeExceeded", null, request.getLocale());
        return ResponseEntity.badRequest()
                .body(resultResponseMapper.failure(Map.of("image", msg)));
    }


    @ExceptionHandler(FileStorageException.class)
    protected ResponseEntity<Object> handleFileStorageException(FileStorageException ex, WebRequest request) {
        log.warn("enter to handleFileStorageException(), message: {}", ex.getMessage());

        String msg = messageSource.getMessage(ex.getMessage(), null, request.getLocale());
        return ResponseEntity.badRequest()
                .body(resultResponseMapper.failure(Map.of("image", msg)));
    }

    @ExceptionHandler(ValidationException.class)
    protected ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest request) {
        log.warn("enter to handleValidationException(), message: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(resultResponseMapper.failure(ex.getErrors()));
    }



    /*@Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.badRequest().build();
    }*/

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn("enter to handleMissingServletRequestPart(), message: {}", ex.getMessage());
        return ResponseEntity.badRequest().build();
    }

// ToDo log 404 errors, and custom page,


}
