package bigmac.urlmodifierbackend.handler;

import bigmac.urlmodifierbackend.dto.ErrorResponse;
import bigmac.urlmodifierbackend.exception.EmailNotExistsException;
import bigmac.urlmodifierbackend.exception.LoginFailException;
import bigmac.urlmodifierbackend.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(EmailAlreadyExistsException e)
    {
        ErrorResponse response = new ErrorResponse("EMAIL_ALREADY_EXISTS", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmailNotExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotExistsException(EmailNotExistsException e)
    {
        ErrorResponse response = new ErrorResponse("EMAIL_NOT_EXISTS", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LoginFailException.class)
    public ResponseEntity<ErrorResponse> handleLoginFailException(LoginFailException e)
    {
        ErrorResponse response = new ErrorResponse("LOGIN_FAIL", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}
