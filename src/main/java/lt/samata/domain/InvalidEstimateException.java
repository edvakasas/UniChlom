package lt.samata.domain;

import java.util.List;

/** Thrown when an estimate request breaks rule R8; carries every problem found, not just the first. */
public class InvalidEstimateException extends RuntimeException {

    private final List<String> errors;

    public InvalidEstimateException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
