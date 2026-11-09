package lt.samata.service;

/** The workshop does not exist, or the estimate does not exist for this workshop. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
