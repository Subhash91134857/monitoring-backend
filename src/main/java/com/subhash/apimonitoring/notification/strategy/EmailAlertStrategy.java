package com.subhash.apimonitoring.notification.strategy;



import com.subhash.apimonitoring.incident.entity.Incident;
import com.subhash.apimonitoring.incident.repository.IncidentRepository;
import com.subhash.apimonitoring.monitoring.entity.MonitoredApi;
import com.subhash.apimonitoring.monitoring.repository.MonitoredApiRepository;
import com.subhash.apimonitoring.notification.dto.NotificationRequest;
import com.subhash.apimonitoring.notification.entity.Alert;
import com.subhash.apimonitoring.notification.enums.AlertChannel;
import com.subhash.apimonitoring.notification.enums.AlertStatus;
import com.subhash.apimonitoring.notification.enums.AlertType;
import com.subhash.apimonitoring.notification.repository.AlertRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

/**
 * Email Alert Strategy
 *
 * WHY - Single Responsibility:
 * Handles ONLY email-specific notification logic
 *
 * HOW:
 * - Uses Thymeleaf templates for HTML emails
 * - Supports both plain text and HTML content
 * - Handles attachments and CC/BCC
 *
 * RESULT: Professional email notifications with branding
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAlertStrategy implements AlertStrategy {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AlertRepository alertRepository;
    private final IncidentRepository incidentRepository;
    private final MonitoredApiRepository apiRepository;
    @Value("${app.notification.email.from}")
    private String fromEmail;

    @Value("${app.notification.email.from-name}")
    private String fromName;

    @Value("${app.notification.email.reply-to}")
    private String replyToEmail;

    @Override
    public Alert send(NotificationRequest request) {
        log.info("Sending email alert to: {}", request.getRecipient());

        Alert alert = createAlertRecord(request);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(String.format("%s <%s>", fromName, fromEmail));
            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject());
            helper.setReplyTo(replyToEmail);
            log.info("Template data = {}", request.getTemplateData());
            // Process HTML template if template data exists
            String emailContent = processEmailContent(request);
            helper.setText(emailContent, true);

            mailSender.send(message);

            alert.setStatus(AlertStatus.SENT);
            alert.setSentAt(LocalDateTime.now());
            alert.setErrorMessage(null);

            log.info("Email alert sent successfully to: {}", request.getRecipient());

        } catch (MessagingException e) {
            log.error("Failed to send email alert to: {}", request.getRecipient(), e);
            alert.setStatus(AlertStatus.FAILED);
            alert.setFailedAt(LocalDateTime.now());
            alert.setErrorMessage(e.getMessage());
        }

        return alertRepository.save(alert);
    }

    private Alert createAlertRecord(NotificationRequest request) {

        Incident incident = null;
        if (request.getIncidentId() != null) {
            incident = incidentRepository.findById(request.getIncidentId())
                    .orElse(null);
        }

        MonitoredApi api = null;
        if (request.getApiId() != null) {
            api = apiRepository.findById(request.getApiId())
                    .orElse(null);
        }

        return Alert.builder()
                .incident(incident)
                .api(api)
                .alertType(request.getAlertType())
                .channel(AlertChannel.EMAIL)
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(AlertStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .build();
    }

    private String processEmailContent(NotificationRequest request) {
        if (request.getTemplateData() != null && !request.getTemplateData().isEmpty()) {
            // Use Thymeleaf template
            Context context = new Context();
            request.getTemplateData().forEach(context::setVariable);

            String templateName = getTemplateName(request.getAlertType());
            return templateEngine.process(templateName, context);
        } else {
            // Plain text content
            return request.getContent();
        }
    }

    private String getTemplateName(AlertType alertType) {
        return switch (alertType) {
            case API_DOWN -> "email/api-down";
            case API_RECOVERED -> "email/api-recovered";
            case INCIDENT_CREATED -> "email/incident-created";
            case INCIDENT_ACKNOWLEDGED -> "email/incident-acknowledged";
            case INCIDENT_RESOLVED -> "email/incident-resolved";
            default -> "email/generic-alert";
        };
    }

    @Override
    public AlertChannel getChannel() {
        return AlertChannel.EMAIL;
    }

    @Override
    public boolean isValidDestination(String destination) {
        return destination != null &&
                destination.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
