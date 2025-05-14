package felosy.authentication;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final String EMAIL_FROM = "20230043@stud.fci-cu.edu.eg";
    private static final String APP_PASSWORD = "nojh svgh hgcp yvty";
    private final String emailTo;
    private final Session session;
    
    // Store OTPs with their expiration time
    private static final Map<String, OTPData> otpStore = new ConcurrentHashMap<>();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private static class OTPData {
        private final String otp;
        private final LocalDateTime expiryTime;
        private boolean used;
        
        public OTPData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.used = false;
        }
    }
    
    public EmailService(String emailTo) {
        this.emailTo = emailTo;
        this.session = getEmailSession();
    }
    
    private static Session getEmailSession() {
        return Session.getInstance(getGmailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });
    }
    
    private static Properties getGmailProperties() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return prop;
    }
    
    /**
     * Generates a new OTP and sends it via email
     * @return true if OTP was generated and sent successfully
     */
    public boolean generateAndSendOTP() {
        String otp = generateOTP();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        
        // Store the OTP
        otpStore.put(emailTo, new OTPData(otp, expiryTime));
        
        // Send the OTP via email
        String subject = "Your OTP Code";
        String htmlBody = String.format("""
            <html>
                <body style='font-family: Arial, sans-serif; padding: 20px;'>
                    <h2>Your OTP Code</h2>
                    <p>Please use the following code to verify your email:</p>
                    <div style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; font-size: 24px; text-align: center; margin: 20px 0;'>
                        <strong>%s</strong>
                    </div>
                    <p>This code will expire in %d minutes.</p>
                    <p>If you didn't request this code, please ignore this email.</p>
                </body>
            </html>
            """, otp, OTP_EXPIRY_MINUTES);
        
        boolean sent = sendHtmlEmail(subject, htmlBody);
        if (sent) {
            LOGGER.info("OTP sent successfully to: " + emailTo);
        } else {
            otpStore.remove(emailTo); // Remove OTP if email sending failed
        }
        return sent;
    }
    
    /**
     * Verifies the provided OTP
     * @param providedOTP The OTP to verify
     * @return true if OTP is valid and not expired
     */
    public boolean verifyOTP(String providedOTP) {
        OTPData otpData = otpStore.get(emailTo);
        
        if (otpData == null) {
            LOGGER.warning("No OTP found for email: " + emailTo);
            return false;
        }
        
        if (otpData.used) {
            LOGGER.warning("OTP already used for email: " + emailTo);
            return false;
        }
        
        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            LOGGER.warning("OTP expired for email: " + emailTo);
            otpStore.remove(emailTo);
            return false;
        }
        
        boolean isValid = otpData.otp.equals(providedOTP);
        if (isValid) {
            otpData.used = true;
            otpStore.remove(emailTo); // Remove OTP after successful verification
            LOGGER.info("OTP verified successfully for email: " + emailTo);
        } else {
            LOGGER.warning("Invalid OTP provided for email: " + emailTo);
        }
        
        return isValid;
    }
    
    /**
     * Generates a random OTP of specified length
     * @return The generated OTP
     */
    private String generateOTP() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
    
    /**
     * Sends a simple text email
     * @param subject Email subject
     * @param body Email body
     * @return true if email was sent successfully
     */
    public boolean sendTextEmail(String subject, String body) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
            message.setSubject(subject);
            message.setText(body);
            
            Transport.send(message);
            LOGGER.info("Text email sent successfully to: " + emailTo);
            return true;
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send text email", e);
            return false;
        }
    }
    
    /**
     * Sends an HTML email
     * @param subject Email subject
     * @param htmlBody HTML content of the email
     * @return true if email was sent successfully
     */
    public boolean sendHtmlEmail(String subject, String htmlBody) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
            message.setSubject(subject);
            
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");
            
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            
            message.setContent(multipart);
            
            Transport.send(message);
            LOGGER.info("HTML email sent successfully to: " + emailTo);
            return true;
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send HTML email", e);
            return false;
        }
    }
    
    /**
     * Sends an email with attachments
     * @param subject Email subject
     * @param body Email body
     * @param attachments Array of files to attach
     * @return true if email was sent successfully
     */
    public boolean sendEmailWithAttachments(String subject, String body, File... attachments) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
            message.setSubject(subject);
            
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            
            for (File file : attachments) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);
                multipart.addBodyPart(attachmentPart);
            }
            
            message.setContent(multipart);
            
            Transport.send(message);
            LOGGER.info("Email with attachments sent successfully to: " + emailTo);
            return true;
        } catch (MessagingException | IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email with attachments", e);
            return false;
        }
    }
    
    /**
     * Sends a verification email with a verification code
     * @param verificationCode The verification code to include in the email
     * @return true if email was sent successfully
     */
    public boolean sendVerificationEmail(String verificationCode) {
        String subject = "Email Verification";
        String htmlBody = String.format("""
            <html>
                <body style='font-family: Arial, sans-serif; padding: 20px;'>
                    <h2>Email Verification</h2>
                    <p>Thank you for registering! Please use the following code to verify your email:</p>
                    <div style='background-color: #f4f4f4; padding: 10px; border-radius: 5px; font-size: 24px; text-align: center; margin: 20px 0;'>
                        <strong>%s</strong>
                    </div>
                    <p>This code will expire in 10 minutes.</p>
                    <p>If you didn't request this verification, please ignore this email.</p>
                </body>
            </html>
            """, verificationCode);
        
        return sendHtmlEmail(subject, htmlBody);
    }
    
    /**
     * Sends a password reset email
     * @param resetLink The password reset link
     * @return true if email was sent successfully
     */
    public boolean sendPasswordResetEmail(String resetLink) {
        String subject = "Password Reset Request";
        String htmlBody = String.format("""
            <html>
                <body style='font-family: Arial, sans-serif; padding: 20px;'>
                    <h2>Password Reset Request</h2>
                    <p>We received a request to reset your password. Click the button below to reset your password:</p>
                    <div style='text-align: center; margin: 20px 0;'>
                        <a href='%s' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>
                            Reset Password
                        </a>
                    </div>
                    <p>If you didn't request a password reset, please ignore this email.</p>
                    <p>This link will expire in 1 hour.</p>
                </body>
            </html>
            """, resetLink);
        
        return sendHtmlEmail(subject, htmlBody);
    }
}
