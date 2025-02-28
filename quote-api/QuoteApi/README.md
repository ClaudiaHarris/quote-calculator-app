# QuoteCalculator API
Spring Boot API for quote calculation and email sending.

## Prerequisites
- JDK 17
- Maven
- Gmail SMTP credentials

## Configure
Add `application.properties` in `src/main/resources`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
