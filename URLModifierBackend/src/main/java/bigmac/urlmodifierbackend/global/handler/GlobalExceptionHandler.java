package bigmac.urlmodifierbackend.global.handler;

import bigmac.urlmodifierbackend.domain.url.exception.URLException;
import bigmac.urlmodifierbackend.domain.user.exception.EmailAlreadyExistsException;
import bigmac.urlmodifierbackend.domain.user.exception.LoginFailException;
import bigmac.urlmodifierbackend.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
        EmailAlreadyExistsException e) {
        ErrorResponse response = new ErrorResponse("EMAIL_ALREADY_EXISTS", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(LoginFailException.class)
    public ResponseEntity<ErrorResponse> handleLoginFailException(LoginFailException e) {
        ErrorResponse response = new ErrorResponse("LOGIN_FAIL", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(URLException.class)
    public ResponseEntity<ErrorResponse> handleURLException(URLException e) {
        ErrorResponse response = new ErrorResponse("URL_Make_Fail", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}
