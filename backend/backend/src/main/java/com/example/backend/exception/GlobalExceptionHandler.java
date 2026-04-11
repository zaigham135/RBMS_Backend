package com.example.backend.exception;

import com.example.backend.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return new ErrorResponse("error", ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new ErrorResponse("error", ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return new ErrorResponse("error", ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return new ErrorResponse("error", "Access denied", LocalDateTime.now());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return new ErrorResponse("error", "Authentication failed", LocalDateTime.now());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials attempt");
        return new ErrorResponse("error", "Invalid email or password", LocalDateTime.now());
    }

    // 400 - @Valid / @Validated failures — returns ALL field errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ErrorResponse("error", errors, LocalDateTime.now());
    }

    // 400 - request body missing or malformed JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return new ErrorResponse("error", "Request body is missing or malformed", LocalDateTime.now());
    }

    // 400 - missing request parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParam(MissingServletRequestParameterException ex) {
        return new ErrorResponse("error", "Required parameter is missing", LocalDateTime.now());
    }

    // 400 - missing path variable
    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingPathVariable(MissingPathVariableException ex) {
        return new ErrorResponse("error", "Required path variable is missing", LocalDateTime.now());
    }

    // 400 - wrong type for path variable or param (e.g. string instead of Long)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ErrorResponse("error", "Invalid parameter type", LocalDateTime.now());
    }

    // 405 - wrong HTTP method (e.g. GET instead of POST)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return new ErrorResponse("error", "HTTP method not allowed", LocalDateTime.now());
    }

    // 415 - unsupported media type (e.g. sending text/plain instead of application/json)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorResponse handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return new ErrorResponse("error", "Content type not supported. Use application/json", LocalDateTime.now());
    }

    // 404 - no handler found (wrong URL)
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoHandlerFound(NoHandlerFoundException ex) {
        return new ErrorResponse("error", "Endpoint not found", LocalDateTime.now());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        log.warn("File upload size exceeded: {}", ex.getMessage());
        return new ErrorResponse("error", "File size exceeds the maximum allowed limit of 20MB. Please upload a smaller file.", LocalDateTime.now());
    }

    // 500 - catch all (always keep this last)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGlobal(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return new ErrorResponse("error", "An internal server error occurred", LocalDateTime.now());
    }
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleDatabaseException(Exception ex) {
        return new ErrorResponse(
                "error",
                "Database error occurred",
                java.time.LocalDateTime.now()
        );
    }
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotWritableException.class)
    public ResponseEntity<ErrorResponse> handleSerialization(Exception ex) {

        return new ResponseEntity<>(
                new ErrorResponse(
                        "error",
                        "Response serialization failed",
                        LocalDateTime.now()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}