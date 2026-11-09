package lt.samata.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Rule R8: rejects a request that cannot produce an estimate, listing every problem at once. */
public class EstimateRequestValidator {

    private static final int MAX_QUANTITY_DECIMALS = 2;

    public void validate(EstimateRequest request, PriceList priceList) {
        List<String> errors = new ArrayList<>();
        checkCarClass(request.carClass(), errors);
        checkWorks(request.workCodes(), priceList, errors);
        request.parts().forEach(part -> checkPart(part, errors));
        if (request.priceLimit() != null && request.priceLimit().signum() < 0) {
            errors.add("Kainos riba negali būti neigiama");
        }
        if (!errors.isEmpty()) {
            throw new InvalidEstimateException(errors);
        }
    }

    private void checkCarClass(String carClass, List<String> errors) {
        boolean known = Arrays.stream(CarClass.values()).anyMatch(value -> value.name().equals(carClass));
        if (!known) {
            errors.add("Nežinoma automobilio klasė: " + carClass);
        }
    }

    private void checkWorks(List<String> workCodes, PriceList priceList, List<String> errors) {
        if (workCodes.isEmpty()) {
            errors.add("Nepasirinktas nė vienas darbas");
        }
        Set<String> seen = new HashSet<>();
        for (String code : workCodes) {
            if (!seen.add(code)) {
                errors.add("Darbo kodas nurodytas du kartus: " + code);
            } else if (priceList.findWork(code).isEmpty()) {
                // another workshop's code is unknown here too: price lists are never mixed
                errors.add("Nežinomas darbo kodas: " + code);
            }
        }
    }

    private void checkPart(EstimateRequest.PartRequest part, List<String> errors) {
        if (part.name() == null || part.name().isBlank()) {
            errors.add("Detalės pavadinimas privalomas");
            return;
        }
        String label = "Detalės „" + part.name() + "“ ";
        BigDecimal quantity = part.qty();
        if (quantity == null || quantity.signum() <= 0) {
            errors.add(label + "kiekis turi būti didesnis už 0");
        } else if (quantity.stripTrailingZeros().scale() > MAX_QUANTITY_DECIMALS) {
            errors.add(label + "kiekis gali turėti ne daugiau kaip 2 skaitmenis po kablelio");
        }
        if (part.unitCost() == null || part.unitCost().signum() < 0) {
            errors.add(label + "pirkimo kaina negali būti neigiama ar tuščia");
        }
    }
}
