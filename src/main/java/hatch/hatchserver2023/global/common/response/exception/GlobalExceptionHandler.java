package hatch.hatchserver2023.global.common.response.exception;

import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

@Slf4j
@RestControllerAdvice // controller 단부터 발생하는 에러 핸들러
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DefaultException.class)
    public ResponseEntity<Object> handleDefaultException(DefaultException e){
        StatusCode code = e.getCode();
        return handleExceptionInternal(code);
    }


    //IllegalArgumentException : cookie 속 jwt 값 잘못되었을 떄
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e){
        String message = e.getMessage();
        CommonCode code = CommonCode.BAD_REQUEST;
        return handleExceptionInternal(code, message);
    }

    // 메서드 수준 보안(@PreAuthorize)에서 발생하는 에러
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException() { //AccessDeniedException e, WebRequest request
        return handleExceptionInternal(UserStatusCode.USER_PRE_FORBIDDEN);
    }

    //@Validated 로 발생하는 에러
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleValidatedException(ConstraintViolationException e){
        CommonCode code = CommonCode.BAD_REQUEST;
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        StringBuilder message = new StringBuilder();
        if(constraintViolations != null) {
            for(ConstraintViolation c : constraintViolations){
                String[] paths = c.getPropertyPath().toString().split("\\.");
                String path = paths.length > 0 ? paths[paths.length - 1] : "";
                message.append(path);
                message.append(" : ");
                message.append(c.getMessage());
                message.append(". ");
            }
        }
        return handleExceptionInternal(code, message.toString());
    }

    //@Valid 로 발생하는 에러
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        CommonCode code = CommonCode.BAD_REQUEST;
        List<ObjectError> errorList = ex.getBindingResult().getAllErrors();

        StringBuilder builder = new StringBuilder();
        errorList.forEach(error -> {
            String field = ( (FieldError) error).getField();
            String msg = error.getDefaultMessage();
            builder.append(field).append(" : ").append(msg).append(". ");
        });
        String message = builder.toString();

        return handleExceptionInternal(code, message);
    }


    // request dto 바인딩 실패 시 발생하는 에러
    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        CommonCode code = CommonCode.BAD_REQUEST;
        List<ObjectError> errorList = ex.getBindingResult().getAllErrors();

        StringBuilder builder = new StringBuilder();
        errorList.forEach(error -> {
            String field = ( (FieldError) error).getField();
            String msg = error.getDefaultMessage();
            builder.append(field).append(" : ").append(msg).append(". ");
        });
        String message = builder.toString();

        return handleExceptionInternal(code, message);
    }


    private ResponseEntity<Object> handleExceptionInternal(StatusCode code){
        CommonResponse errorResponse = CommonResponse.toErrorResponse(code);
        return ResponseEntity
                .status(code.getStatus())
                .body(errorResponse);
    }

    private ResponseEntity<Object> handleExceptionInternal(StatusCode code, String message){
        CommonResponse errorResponse = CommonResponse.toErrorResponse(code, message);
        return ResponseEntity
                .status(code.getStatus())
                .body(errorResponse);
    }
}