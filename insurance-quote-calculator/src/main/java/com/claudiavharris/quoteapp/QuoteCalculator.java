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

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuoteCalculator extends JFrame {
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
        accidentCheckBox.setToolTipText("Check if you’ve had accidents in the last 5 years");
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        buttonPanel.setBackground(new Color(220, 220, 220));
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

        agentLabel = new JLabel("");
        agentLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        buttonPanel.add(calculateButton);
        buttonPanel.add(contactAgentButton);
        buttonPanel.add(emailQuoteButton);
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

            String json = String.format("{\"age\":%d,\"years\":%d,\"accidents\":%b}", age, years, hasAccidents);
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("https://quote-api-claudia-4d1423dc8823.herokuapp.com/api/calculate-quote")
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            String responseBody = response.body().string();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Parse JSON response
            String[] parts = responseBody.replace("{", "").replace("}", "").split(",");
            double basePremium = Double.parseDouble(parts[0].split(":")[1]);
            double fullPaymentDiscount = Double.parseDouble(parts[1].split(":")[1]);
            double downPayment = Double.parseDouble(parts[2].split(":")[1]);
            double remainingBalance = Double.parseDouble(parts[3].split(":")[1]);
            double monthlyPayment = Double.parseDouble(parts[4].split(":")[1]);

            basePremiumLabel.setText(String.format("Total Premium: $%.2f", basePremium));
            fullPaymentLabel.setText(String.format("Full Payment (5%% off): $%.2f", fullPaymentDiscount));
            downPaymentLabel.setText(String.format("Down Payment (10%%): $%.2f", downPayment));
            remainingLabel.setText(String.format("Remaining Balance: $%.2f", remainingBalance));
            monthlyLabel.setText(String.format("6 Monthly Payments: $%.2f", monthlyPayment));

            agentLabel.setText("");
            contactAgentButton.setEnabled(true);
            emailQuoteButton.setEnabled(true);
        } catch (Exception ex) {
            agentLabel.setText("Error: " + ex.getMessage());
            contactAgentButton.setEnabled(false);
            emailQuoteButton.setEnabled(false);
        }
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
                String json = String.format(
                        "{\"email\":\"%s\",\"details\":{\"basePremium\":%.2f,\"fullPaymentDiscount\":%.2f,\"downPayment\":%.2f,\"remainingBalance\":%.2f,\"monthlyPayment\":%.2f}}",
                        email,
                        Double.parseDouble(basePremiumLabel.getText().split("\\$")[1]),
                        Double.parseDouble(fullPaymentLabel.getText().split("\\$")[1]),
                        Double.parseDouble(downPaymentLabel.getText().split("\\$")[1]),
                        Double.parseDouble(remainingLabel.getText().split("\\$")[1]),
                        Double.parseDouble(monthlyLabel.getText().split("\\$")[1])
                );
                RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url("https://quote-api-claudia-4d1423dc8823.herokuapp.com/api/email-quote")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                JOptionPane.showMessageDialog(this, responseBody, "Email Sent", JOptionPane.INFORMATION_MESSAGE);
                contactAgentButton.setEnabled(true);
                emailQuoteButton.setEnabled(false);
                break;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error sending email: " + ex.getMessage(), "Email Error", JOptionPane.ERROR_MESSAGE);
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

class PremiumDetails {
    double basePremium, fullPaymentDiscount, downPayment, remainingBalance, monthlyPayment;
    public PremiumDetails() {} // Default constructor for Jackson
    PremiumDetails(double base, double full, double down, double remaining, double monthly) {
        this.basePremium = base;
        this.fullPaymentDiscount = full;
        this.downPayment = down;
        this.remainingBalance = remaining;
        this.monthlyPayment = monthly;
    }
    public double getBasePremium() { return basePremium; }
    public double getFullPaymentDiscount() { return fullPaymentDiscount; }
    public double getDownPayment() { return downPayment; }
    public double getRemainingBalance() { return remainingBalance; }
    public double getMonthlyPayment() { return monthlyPayment; }
    public void setBasePremium(double basePremium) { this.basePremium = basePremium; }
    public void setFullPaymentDiscount(double fullPaymentDiscount) { this.fullPaymentDiscount = fullPaymentDiscount; }
    public void setDownPayment(double downPayment) { this.downPayment = downPayment; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }
    public void setMonthlyPayment(double monthlyPayment) { this.monthlyPayment = monthlyPayment; }
}
