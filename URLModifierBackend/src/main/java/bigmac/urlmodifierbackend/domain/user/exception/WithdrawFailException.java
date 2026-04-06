package bigmac.urlmodifierbackend.domain.user.exception;

public class WithdrawFailException extends RuntimeException {
    public WithdrawFailException(String message) {
        super(message);
    }
}
