package bigmac.urlmodifierbackend.domain.user.exception;

public class LoginFailException extends RuntimeException {
    public LoginFailException(String message)
    {
        super(message);
    }
}