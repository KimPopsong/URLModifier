package bigmac.urlmodifierbackend.handler;

import bigmac.urlmodifierbackend.dto.ErrorResponse;
import bigmac.urlmodifierbackend.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e)
    {
        ErrorResponse response = new ErrorResponse("이미 존재하는 이메일입니다.", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}
