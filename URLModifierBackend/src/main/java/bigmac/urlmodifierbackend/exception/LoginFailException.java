package bigmac.urlmodifierbackend.exception;

public class LoginFailException extends RuntimeException {
    public LoginFailException(String message)
    {
        super(message);
    }
}