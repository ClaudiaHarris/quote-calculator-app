package com.claudiavharris.quoteapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")  // This makes all endpoints start with /api
public class QuoteController {
    private static final Logger LOGGER = Logger.getLogger(QuoteController.class.getName());
    private final JavaMailSender mailSender;
    public QuoteController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    @GetMapping("/")  // Handles requests to "/api/"
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Quote API is running!");
    }

    @PostMapping("/calculate-quote")  // Handles POST requests to "/api/calculate-quote"
    public ResponseEntity<String> calculateQuote(@RequestBody QuoteRequest request) {
        double quote = calculateInsuranceQuote(request.getAge(), request.getYears(), request.isAccidents());
        return ResponseEntity.ok("Your insurance quote is: $" + quote);
    }

    private double calculateInsuranceQuote(int age, int years, boolean hasAccidents) {
        double basePrice = 500.0;
        if (age < 25) basePrice += 200;
        if (years > 5) basePrice -= 50;
        if (hasAccidents) basePrice += 150;
        return basePrice;
    }

    @PostMapping("/email-quote")
    public ResponseEntity<String> emailQuote(@RequestBody EmailRequest request) {
        LOGGER.info("Received /email-quote request");
        if (request == null || request.getDetails() == null || request.getEmail() == null) {
            LOGGER.warning("Invalid request data");
            throw new IllegalArgumentException("Invalid request data");
        }
        PremiumDetails details = request.getDetails();
        String email = request.getEmail();
        LOGGER.info("Preparing email for: " + email);
        String quote = String.format(
                "Your Insurance Quote:\n" +
                        "Total Premium: $%.2f\n" +
                        "Full Payment (5%% off): $%.2f\n" +
                        "Down Payment (10%%): $%.2f\n" +
                        "Remaining Balance: $%.2f\n" +
                        "6 Monthly Payments: $%.2f each",
                details.getBasePremium(), details.getFullPaymentDiscount(), details.getDownPayment(),
                details.getRemainingBalance(), details.getMonthlyPayment()
        );

        try {
            LOGGER.info("Sending email ...");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("hello@claudiavharris.com");
            message.setTo(email);
            message.setSubject("Your Insurance Quote");
            message.setText(quote);
            mailSender.send(message);
            LOGGER.info("Email sent successfully");
            return ResponseEntity.ok("Quote emailed to " + email);
        } catch (Exception ex) {
            LOGGER.severe("Failed to send email: " + ex.getMessage());
            throw new RuntimeException("Failed to send email: " + ex.getMessage(), ex);
        }
    }
}

