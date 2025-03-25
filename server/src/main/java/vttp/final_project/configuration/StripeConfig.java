package vttp.final_project.configuration;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        System.out.println("Initializing Stripe with API key: " +
                           (stripeApiKey != null && !stripeApiKey.isEmpty() ?
                                   stripeApiKey.substring(0, 5) + "..." : "NOT SET"));
        Stripe.apiKey = stripeApiKey;
    }
}
