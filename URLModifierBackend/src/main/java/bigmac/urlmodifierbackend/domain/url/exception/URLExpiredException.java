package bigmac.urlmodifierbackend.domain.url.exception;

public class URLExpiredException extends RuntimeException {
    public URLExpiredException(String message) {
        super(message);
    }
}
