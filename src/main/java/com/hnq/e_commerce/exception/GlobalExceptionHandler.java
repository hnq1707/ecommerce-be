package com.hnq.e_commerce.exception;

import com.hnq.e_commerce.auth.exceptions.ErrorCode;
import com.hnq.e_commerce.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    /**
     * Xử lý các ngoại lệ chưa được phân loại
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException exception) {
        log.error("Uncategorized exception occurred", exception);
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                        .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                        .build());
    }

    /**
     * Xử lý ngoại lệ ResourceNotFoundEx
     */
    @ExceptionHandler(ResourceNotFoundEx.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundEx exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("Resource not found: {}", errorCode.getMessage());
        
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    /**
     * Xử lý ngoại lệ truy cập bị từ chối
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        log.warn("Access denied: {}", exception.getMessage());
        
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    /**
     * Xử lý lỗi validation từ các đối số phương thức
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String enumKey = Optional.ofNullable(exception.getFieldError())
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("");
        
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = new HashMap<>();
        
        try {
            errorCode = ErrorCode.valueOf(enumKey);
            
            ConstraintViolation<?> constraintViolation = exception.getBindingResult()
                    .getAllErrors()
                    .stream()
                    .findFirst()
                    .map(error -> error.unwrap(ConstraintViolation.class))
                    .orElse(null);
            
            if (constraintViolation != null) {
                attributes = constraintViolation.getConstraintDescriptor().getAttributes();
                log.debug("Validation attributes: {}", attributes);
            }
        } catch (IllegalArgumentException e) {
            log.debug("Could not map error message to ErrorCode enum: {}", enumKey);
        }
        
        String finalMessage = attributes.isEmpty() 
                ? errorCode.getMessage() 
                : formatMessage(errorCode.getMessage(), attributes);
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(finalMessage)
                        .build());
    }

    /**
     * Định dạng thông báo lỗi với các thuộc tính từ ràng buộc validation
     */
    private String formatMessage(String message, Map<String, Object> attributes) {
        if (attributes.containsKey(MIN_ATTRIBUTE)) {
            String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
            return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
        }
        return message;
    }
}