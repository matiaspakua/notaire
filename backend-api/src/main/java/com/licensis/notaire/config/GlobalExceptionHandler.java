package com.licensis.notaire.config;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ErrorResponse;
import com.licensis.notaire.exception.NotaireException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.observability.StructuredLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Provides consistent error responses with structured logging.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final StructuredLogger STRUCTURED_LOG = StructuredLogger.getInstance();

    /**
     * Handle NotaireException and its subclasses.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return error response entity
     */
    @ExceptionHandler({NotaireException.class})
    public ResponseEntity<ErrorResponse> handleNotaireException(
            NotaireException ex,
            HttpServletRequest request) {
        int statusCode = ex.getStatusCode();
        ErrorResponse errorResponse = new ErrorResponse(
            statusCode,
            HttpStatus.valueOf(statusCode).getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        STRUCTURED_LOG.logError(
            "Notaire exception occurred",
            ex,
            Map.of(
                "statusCode", statusCode,
                "path", request.getRequestURI(),
                "method", request.getMethod()
            )
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(statusCode));
    }

    /**
     * Handle ResourceNotFoundException.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return 404 error response entity
     */
    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            404,
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        STRUCTURED_LOG.logWarn(
            "Resource not found",
            Map.of(
                "path", request.getRequestURI(),
                "message", ex.getMessage()
            )
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle BusinessValidationException.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return 400 error response entity
     */
    @ExceptionHandler({BusinessValidationException.class})
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(
            BusinessValidationException ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            400,
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        STRUCTURED_LOG.logWarn(
            "Business validation failed",
            Map.of(
                "path", request.getRequestURI(),
                "message", ex.getMessage()
            )
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle bean validation failures on {@code @RequestBody} arguments (issue #561).
     * Overrides the superclass's default {@code ProblemDetail} handling to keep the
     * project's own {@link ErrorResponse} shape consistent across all error responses.
     *
     * @param ex      the exception
     * @param headers the HTTP headers
     * @param status  the HTTP status
     * @param request the web request
     * @return 400 error response entity
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = new ErrorResponse(
            400,
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message,
            path
        );
        STRUCTURED_LOG.logWarn(
            "Request body validation failed",
            Map.of(
                "path", path,
                "message", message
            )
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle bean validation failures on {@code @RequestParam}/{@code @PathVariable}
     * arguments, raised when the controller class is annotated {@code @Validated} (issue #561).
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return 400 error response entity
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        ErrorResponse errorResponse = new ErrorResponse(
            400,
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message,
            request.getRequestURI()
        );
        STRUCTURED_LOG.logWarn(
            "Request parameter validation failed",
            Map.of(
                "path", request.getRequestURI(),
                "message", message
            )
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violations raised by the persistence layer (e.g. NOT NULL,
     * unique, or foreign key violations) as a client error rather than a bare 500,
     * since they stem from the request payload, not a server fault.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return 400 error response entity
     */
    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            400,
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Los datos enviados no cumplen las restricciones de la base de datos",
            request.getRequestURI()
        );
        STRUCTURED_LOG.logWarn(
            "Data integrity violation",
            Map.of(
                "path", request.getRequestURI(),
                "method", request.getMethod()
            )
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions.
     *
     * @param ex      the exception
     * @param request the HTTP request
     * @return 500 error response entity
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            500,
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "An unexpected error occurred",
            request.getRequestURI()
        );
        STRUCTURED_LOG.logError(
            "Unexpected exception occurred",
            ex,
            Map.of(
                "path", request.getRequestURI(),
                "method", request.getMethod(),
                "exceptionType", ex.getClass().getSimpleName()
            )
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
