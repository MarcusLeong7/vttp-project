package vttp.final_project.services;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    @Value("${app.frontend.url}")
    private String baseUrl;

    public String getPublicKey() {
        return stripePublicKey;
    }

    public Session createCheckoutSession(String email, long amount, String productName) throws StripeException {
        // Create a new Checkout Session for the order
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(email)
                .setSuccessUrl(baseUrl + "/#/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amount) // amount in cents
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(productName)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        return Session.create(params);
    }

    public boolean verifyPaymentSuccess(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        return "complete".equals(session.getStatus()) || "paid".equals(session.getPaymentStatus());
    }
}

