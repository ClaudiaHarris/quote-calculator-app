package com.claudiavharris.quoteapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.claudiavharris.shared.EmailRequest;
import com.claudiavharris.shared.PremiumDetails;
import com.claudiavharris.shared.QuoteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuoteCalculator extends JFrame {
    private static final String API_BASE_URL = "https://quote-api-claudia-4d1423dc8823.herokuapp.com/api";
    private JSpinner ageSpinner, drivingYearsSpinner;
    private JCheckBox accidentCheckBox;
    private JLabel basePremiumLabel, fullPaymentLabel, downPaymentLabel, remainingLabel, monthlyLabel, agentLabel;
    private JButton contactAgentButton, emailQuoteButton;
    private final OkHttpClient client = new OkHttpClient();

    public QuoteCalculator() {
        setTitle("Insurance Quote Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(new Color(220, 220, 220));
        JLabel titleLabel = new JLabel("Enter Your Details", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        inputPanel.add(titleLabel);
        inputPanel.add(new JLabel(""));

        inputPanel.add(new JLabel("Age:"));
        ageSpinner = new JSpinner(new SpinnerNumberModel(18, 16, 120, 1));
        ageSpinner.setToolTipText("Your age (16-120)");
        inputPanel.add(ageSpinner);

        inputPanel.add(new JLabel("Years Driving:"));
        drivingYearsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        drivingYearsSpinner.setToolTipText("Years of driving experience (0-100)");
        inputPanel.add(drivingYearsSpinner);

        inputPanel.add(new JLabel("Accidents?"));
        accidentCheckBox = new JCheckBox();
        accidentCheckBox.setToolTipText("Check if you've had accidents in the last 5 years");
        inputPanel.add(accidentCheckBox);
        add(inputPanel, BorderLayout.NORTH);

        JPanel outputPanel = new JPanel(new GridLayout(5, 1, 0, 10));
        outputPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        outputPanel.setBackground(new Color(240, 240, 240));
        outputPanel.setPreferredSize(new Dimension(350, 150));
        basePremiumLabel = new JLabel("Total Premium: $0.00");
        fullPaymentLabel = new JLabel("Full Payment (5% off): $0.00");
        downPaymentLabel = new JLabel("Down Payment (10%): $0.00");
        remainingLabel = new JLabel("Remaining Balance: $0.00");
        monthlyLabel = new JLabel("6 Monthly Payments: $0.00");
        JLabel[] labels = {basePremiumLabel, fullPaymentLabel, downPaymentLabel, remainingLabel, monthlyLabel};
        for (JLabel label : labels) {
            label.setFont(new Font("Arial", Font.PLAIN, 13));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            outputPanel.add(label);
        }
        add(outputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5)); // Change to 2 rows, 1 column
        buttonPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        buttonPanel.setBackground(new Color(220, 220, 220));

        // Create a sub-panel for buttons
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonsRow.setBackground(new Color(220, 220, 220));

        JButton calculateButton = new JButton("Calculate Quote");
        calculateButton.setPreferredSize(new Dimension(120, 30));
        calculateButton.setToolTipText("Calculate your insurance quote");

        contactAgentButton = new JButton("Contact Agent");
        contactAgentButton.setPreferredSize(new Dimension(120, 30));
        contactAgentButton.setToolTipText("Request agent contact after viewing quote");
        contactAgentButton.setEnabled(false);

        emailQuoteButton = new JButton("Email Quote");
        emailQuoteButton.setPreferredSize(new Dimension(120, 30));
        emailQuoteButton.setToolTipText("Email your quote to yourself");
        emailQuoteButton.setEnabled(false);

        // Add buttons to the buttons row
        buttonsRow.add(calculateButton);
        buttonsRow.add(contactAgentButton);
        buttonsRow.add(emailQuoteButton);

        // Setup agent label with proper sizing
        agentLabel = new JLabel("");
        agentLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        agentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        agentLabel.setPreferredSize(new Dimension(400, 20));

        // Add components to button panel
        buttonPanel.add(buttonsRow);
        buttonPanel.add(agentLabel);
        add(buttonPanel, BorderLayout.SOUTH);

        calculateButton.addActionListener(e -> calculateAndDisplay());
        contactAgentButton.addActionListener(e -> showAgentPrompt());
        emailQuoteButton.addActionListener(e -> sendQuoteEmail());
        pack();
        setMinimumSize(new Dimension(400, 400));
    }

    private void calculateAndDisplay() {
        try {
            int age = (Integer) ageSpinner.getValue();
            int years = (Integer) drivingYearsSpinner.getValue();
            if (age < 0 || years < 0) throw new NumberFormatException();
            boolean hasAccidents = accidentCheckBox.isSelected();

            // Create QuoteRequest object and convert to JSON
            QuoteRequest quoteRequest = new QuoteRequest();
            quoteRequest.setAge(age);
            quoteRequest.setYears(years);
            quoteRequest.setAccidents(hasAccidents);
            
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(quoteRequest);
            
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/calculate-quote")
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new IOException("API Error: " + responseBody);
            }

            // Check content type
            String contentType = response.header("Content-Type", "");
            if (contentType.contains("application/json")) {
                // Parse JSON response
                PremiumDetails details = mapper.readValue(responseBody, PremiumDetails.class);
                updateLabels(details);
            } else {
                // Handle text response
                // Expected format: "Your insurance quote is: $700.0"
                String amount = responseBody.split("\\$")[1].trim();
                double premium = Double.parseDouble(amount);
                updateLabelsFromPremium(premium);
            }

            agentLabel.setText("");
            contactAgentButton.setEnabled(true);
            emailQuoteButton.setEnabled(true);
        } catch (Exception ex) {
            agentLabel.setText("Error: " + ex.getMessage());
            contactAgentButton.setEnabled(false);
            emailQuoteButton.setEnabled(false);
            ex.printStackTrace();
        }
    }

    private void updateLabels(PremiumDetails details) {
        basePremiumLabel.setText(String.format("Total Premium: $%.2f", details.getBasePremium()));
        fullPaymentLabel.setText(String.format("Full Payment (5%% off): $%.2f", details.getFullPaymentDiscount()));
        downPaymentLabel.setText(String.format("Down Payment (10%%): $%.2f", details.getDownPayment()));
        remainingLabel.setText(String.format("Remaining Balance: $%.2f", details.getRemainingBalance()));
        monthlyLabel.setText(String.format("6 Monthly Payments: $%.2f", details.getMonthlyPayment()));
    }

    private void updateLabelsFromPremium(double premium) {
        double fullPayment = premium * 0.95;
        double downPayment = premium * 0.10;
        double remaining = premium - downPayment;
        double monthly = remaining / 6;

        basePremiumLabel.setText(String.format("Total Premium: $%.2f", premium));
        fullPaymentLabel.setText(String.format("Full Payment (5%% off): $%.2f", fullPayment));
        downPaymentLabel.setText(String.format("Down Payment (10%%): $%.2f", downPayment));
        remainingLabel.setText(String.format("Remaining Balance: $%.2f", remaining));
        monthlyLabel.setText(String.format("6 Monthly Payments: $%.2f", monthly));
    }

    private void showAgentPrompt() {
        // Unchanged local logic
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField nameField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        String[] contactMethods = {"Phone", "Email"};
        JComboBox<String> contactMethodBox = new JComboBox<>(contactMethods);

        infoPanel.add(new JLabel("Name:"));
        infoPanel.add(nameField);
        infoPanel.add(new JLabel("Phone:"));
        infoPanel.add(phoneField);
        infoPanel.add(new JLabel("Email:"));
        infoPanel.add(emailField);
        infoPanel.add(new JLabel("Preferred Contact:"));
        infoPanel.add(contactMethodBox);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, infoPanel, "Enter Contact Details", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) {
                JOptionPane.showMessageDialog(this, "Agent request canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
                contactAgentButton.setEnabled(false);
                emailQuoteButton.setEnabled(true);
                return;
            }

            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String contactMethod = (String) contactMethodBox.getSelectedItem();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Name is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (phone.isEmpty() && email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: At least one contact method required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!phone.isEmpty() && !phone.matches("\\d{3}-\\d{3}-\\d{4}")) {
                JOptionPane.showMessageDialog(this, "Error: Phone must be XXX-XXX-XXXX.", "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!email.isEmpty() && !email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
                JOptionPane.showMessageDialog(this, "Error: Invalid email format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            String contactInfo = phone.isEmpty() ? email : phone;
            String message = "Agent will contact " + name + " via " + contactMethod.toLowerCase() + " at " + contactInfo;
            JOptionPane.showMessageDialog(this, message, "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
            contactAgentButton.setEnabled(false);
            emailQuoteButton.setEnabled(true);
            break;
        }
    }

    private void sendQuoteEmail() {
        JPanel emailPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        emailPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField emailField = new JTextField(15);
        emailPanel.add(new JLabel("Email:"));
        emailPanel.add(emailField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, emailPanel, "Enter Email for Quote", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) {
                JOptionPane.showMessageDialog(this, "Email request canceled.", "Canceled", JOptionPane.INFORMATION_MESSAGE);
                contactAgentButton.setEnabled(true);
                emailQuoteButton.setEnabled(false);
                return;
            }

            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Email is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
                JOptionPane.showMessageDialog(this, "Error: Invalid email format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                if (basePremiumLabel.getText().equals("Total Premium: $0.00")) {
                    JOptionPane.showMessageDialog(this, "Calculate a quote first!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create EmailRequest object
                EmailRequest emailRequest = new EmailRequest();
                emailRequest.setEmail(email);

                // Create PremiumDetails object from labels
                PremiumDetails details = new PremiumDetails();
                details.setBasePremium(Double.parseDouble(basePremiumLabel.getText().split("\\$")[1]));
                details.setFullPaymentDiscount(Double.parseDouble(fullPaymentLabel.getText().split("\\$")[1]));
                details.setDownPayment(Double.parseDouble(downPaymentLabel.getText().split("\\$")[1]));
                details.setRemainingBalance(Double.parseDouble(remainingLabel.getText().split("\\$")[1]));
                details.setMonthlyPayment(Double.parseDouble(monthlyLabel.getText().split("\\$")[1]));

                emailRequest.setDetails(details);

                // Convert to JSON using ObjectMapper
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(emailRequest);

                RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url(API_BASE_URL + "/email-quote")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                
                if (!response.isSuccessful()) throw new IOException("API Error: " + responseBody);
                
                JOptionPane.showMessageDialog(this, responseBody, "Email Sent", JOptionPane.INFORMATION_MESSAGE);
                contactAgentButton.setEnabled(true);
                emailQuoteButton.setEnabled(false);
                break;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error sending email: " + ex.getMessage(), "Email Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QuoteCalculator frame = new QuoteCalculator();
            frame.setVisible(true);
        });
    }
}
