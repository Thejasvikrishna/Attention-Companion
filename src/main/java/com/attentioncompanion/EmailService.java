package com.attentioncompanion;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.util.Properties;

public class EmailService {

    // Default sender (your Gmail + app password)
    public static final String SENDER_EMAIL = "";
    public static final String APP_PASSWORD = "";

    // Existing report email (unchanged)
    public static boolean sendReportEmail(String toEmail, String smtpUser, String smtpPassword, String csvFile, String pdfFile) {
        try {
            if (!new File(csvFile).exists() || !new File(pdfFile).exists()) {
                System.err.println("Report files missing");
                return false;
            }

            MultiPartEmail email = new MultiPartEmail();
            email.setHostName("smtp.gmail.com");  // ← FIXED: Hardcode Gmail SMTP
            email.setSmtpPort(587);               // ← FIXED: Port 587 for TLS
            email.setAuthentication(smtpUser, smtpPassword);
            email.setSSLOnConnect(true);
            email.setStartTLSEnabled(true);       // ← ADD THIS

            email.setFrom(smtpUser, "Attention Companion");
            email.setSubject("Study Session Report");
            email.setMsg("Attached are your CSV and PDF reports.");

            EmailAttachment a1 = new EmailAttachment();
            a1.setPath(csvFile);
            a1.setDisposition(EmailAttachment.ATTACHMENT);
            email.attach(a1);

            EmailAttachment a2 = new EmailAttachment();
            a2.setPath(pdfFile);
            a2.setDisposition(EmailAttachment.ATTACHMENT);
            email.attach(a2);

            email.addTo(toEmail);
            email.send();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Single alert email method (keep ONLY this one)
    public static boolean sendAlertEmail(String to, String subject, String body) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(
                    props,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
                        }
                    }
            );

            Message message = new javax.mail.internet.MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            javax.mail.Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
