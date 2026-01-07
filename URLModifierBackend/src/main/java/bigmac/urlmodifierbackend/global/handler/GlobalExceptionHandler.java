package bigmac.urlmodifierbackend.global.handler;

import bigmac.urlmodifierbackend.domain.url.exception.URLException;
import bigmac.urlmodifierbackend.domain.user.exception.EmailAlreadyExistsException;
import bigmac.urlmodifierbackend.domain.user.exception.LoginFailException;
import bigmac.urlmodifierbackend.domain.user.exception.URLFindException;
import bigmac.urlmodifierbackend.global.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
        EmailAlreadyExistsException e) {
        log.warn("Email already exists: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("EMAIL_ALREADY_EXISTS", 
            "이미 사용 중인 이메일입니다.");

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(LoginFailException.class)
    public ResponseEntity<ErrorResponse> handleLoginFailException(LoginFailException e) {
        log.warn("Login failed: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("LOGIN_FAIL", 
            "이메일 또는 비밀번호가 올바르지 않습니다.");

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(URLException.class)
    public ResponseEntity<ErrorResponse> handleURLException(URLException e) {
        log.error("URL error: {}", e.getMessage(), e);
        ErrorResponse response = new ErrorResponse("URL_MAKE_FAIL", 
            "URL 단축 중 오류가 발생했습니다. 다시 시도해주세요.");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(URLFindException.class)
    public ResponseEntity<ErrorResponse> handleURLFindException(URLFindException e) {
        log.warn("URL not found: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("URL_FIND_FAIL", 
            "요청하신 URL을 찾을 수 없습니다.");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        log.warn("Response status exception: {}", e.getMessage());
        String userMessage = switch (e.getStatusCode().value()) {
            case 401 -> "인증이 필요합니다. 로그인 후 다시 시도해주세요.";
            case 403 -> "접근 권한이 없습니다.";
            case 404 -> "요청하신 리소스를 찾을 수 없습니다.";
            default -> "요청 처리 중 오류가 발생했습니다. 다시 시도해주세요.";
        };
        
        ErrorResponse response = new ErrorResponse("REQUEST_ERROR", userMessage);
        return new ResponseEntity<>(response, e.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", 
            "입력값이 올바르지 않습니다. 다시 확인해주세요.");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);
        ErrorResponse response = new ErrorResponse("INTERNAL_ERROR", 
            "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
