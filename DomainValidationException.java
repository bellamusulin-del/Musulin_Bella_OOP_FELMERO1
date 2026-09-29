package carservice;

/**
 * Custom CHECKED exception. It is thrown when the data is invalid
 * (missing required field, negative price, unknown work order type, ...).
 * Because it extends Exception (not RuntimeException), the compiler
 * forces us to catch it or declare it with "throws".
 */
public class DomainValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
