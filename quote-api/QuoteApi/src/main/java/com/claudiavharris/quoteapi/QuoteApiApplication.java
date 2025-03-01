package com.claudiavharris.quoteapi;

import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class QuoteApiApplication {
    private static final Logger LOGGER = Logger.getLogger(QuoteApiApplication.class.getName());
    private final JavaMailSender mailSender;

    public QuoteApiApplication(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public static void main(String[] args) {
        SpringApplication.run(QuoteApiApplication.class, args);
    }

    @PostMapping("/calculate-quote")
    public PremiumDetails calculateQuote(@RequestBody QuoteRequest request) {
        int age = request.getAge();
        int years = request.getYears();
        boolean accidents = request.isAccidents();
        double basePremium = 500.0;
        if (age < 25) basePremium += 200.0;
        else if (age > 65) basePremium += 100.0;
        if (years < 5) basePremium += 150.0;
        if (accidents) basePremium += 300.0;
        return new PremiumDetails(basePremium, basePremium * 0.95, basePremium * 0.10,
                basePremium * 0.9, basePremium * 0.9 / 6);
    }

    @PostMapping("/email-quote")
    public String emailQuote(@RequestBody EmailRequest request) {
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
            return "Quote emailed to " + email;
        } catch (Exception ex) {
            LOGGER.severe("Failed to send email: " + ex.getMessage());
            throw new RuntimeException("Failed to send email" + ex.getMessage(), ex);
        }
    }
}