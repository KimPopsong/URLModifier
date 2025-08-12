package bigmac.urlmodifierbackend.domain.user.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message)
    {
        super(message);
    }
}