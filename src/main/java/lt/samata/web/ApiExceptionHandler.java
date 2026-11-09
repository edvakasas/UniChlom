package lt.samata.web;

import lt.samata.domain.InvalidEstimateException;
import lt.samata.persistence.EstimateStateException;
import lt.samata.service.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Turns errors into HTTP answers with a list of messages the user can read. */
@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorResponse(List<String> errors) {
    }

    @ExceptionHandler(InvalidEstimateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse invalidEstimate(InvalidEstimateException e) {
        return new ErrorResponse(e.getErrors());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse notFound(NotFoundException e) {
        return new ErrorResponse(List.of(e.getMessage()));
    }

    @ExceptionHandler(EstimateStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse wrongState(EstimateStateException e) {
        return new ErrorResponse(List.of(e.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse missingTenant(MissingRequestHeaderException e) {
        return new ErrorResponse(List.of("Nenurodytas servisas: trūksta antraštės " + EstimateController.TENANT_HEADER));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse unreadableBody(HttpMessageNotReadableException e) {
        return new ErrorResponse(List.of("Neteisingas užklausos formatas (JSON)"));
    }
}
