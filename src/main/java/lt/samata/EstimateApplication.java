package lt.samata;

import lt.samata.domain.EstimateCalculator;
import lt.samata.domain.EstimateRequestValidator;
import lt.samata.domain.discount.PromotionDiscount;
import lt.samata.domain.discount.RegularCustomerDiscount;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class EstimateApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstimateApplication.class, args);
    }

    /** The core module knows nothing about Spring; it is wired here with the discount rules in use. */
    @Bean
    EstimateCalculator estimateCalculator() {
        return new EstimateCalculator(new EstimateRequestValidator(),
                List.of(new PromotionDiscount(), new RegularCustomerDiscount()));
    }
}
