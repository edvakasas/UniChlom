package lt.samata.persistence;

/** An action that the estimate's current status does not allow (e.g. approving it twice). */
public class EstimateStateException extends RuntimeException {

    public EstimateStateException(String message) {
        super(message);
    }
}
