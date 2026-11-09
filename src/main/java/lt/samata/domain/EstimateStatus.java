package lt.samata.domain;

public enum EstimateStatus {
    /** Ready: no price limit, or the total is within it. */
    PARENGTA,
    /** Total is above the customer's price limit, so the customer has to approve it. */
    LAUKIA_PATVIRTINIMO,
    /** The customer approved an estimate that was above the limit. */
    PATVIRTINTA
}
