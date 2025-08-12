package bigmac.urlmodifierbackend.domain.user.exception;

public class EmailNotExistsException extends RuntimeException {
    public EmailNotExistsException(String message)
    {
        super(message);
    }
}