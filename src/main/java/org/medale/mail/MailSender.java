package org.medale.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 */
public class MailSender {

    public static final String HTML_UTF8_MIME_TYPE = "text/html; charset=utf-8";
    public static final String RECIPIENT_SEPARATOR = ",";
    public static final String MESSAGE_SUBJECT = "Exsiter backup report";
    public static final String PROP_SMTP_USERNAME = "smtp.username";
    public static final String PROP_SMTP_PASSWORD = "smtp.password";
    public static final String JAVA_MAIL_PROP_PREFIX = "mail.";
    public static final String PROP_SMTP_SENDER = "smtp.sender";
    public static final String PROP_SMTP_RECIPIENTS = "smtp.recipients";

    private String smtpUsername;
    private String smtpPassword;
    private String smtpSender;
    private Properties javaMailProps;
    private String smtpRecipients;
    private final List<String> reports;

    public MailSender() {
        this.reports = new ArrayList<String>();
    }

    public String getSmtpUsername() {
        return this.smtpUsername;
    }

    public String getSmtpPassword() {
        return this.smtpPassword;
    }

    public String getSmtpSender() {
        return this.smtpSender;
    }

    public String getSmtpRecipients() {
        return this.smtpRecipients;
    }

    public List<String> getReports() {
        return this.reports;
    }

    public void addReport(final String report) {
        this.reports.add(report);
    }

    public void configure(final Properties configProps) {
        this.smtpUsername = getRequiredPropertyOrThrowException(configProps,
                PROP_SMTP_USERNAME);
        this.smtpPassword = getRequiredPropertyOrThrowException(configProps,
                PROP_SMTP_PASSWORD);
        this.smtpSender = getRequiredPropertyOrThrowException(configProps,
                PROP_SMTP_SENDER);
        this.smtpRecipients = getRequiredPropertyOrThrowException(configProps,
                PROP_SMTP_RECIPIENTS);
        this.javaMailProps = getJavaMailProps(configProps);
    }

    protected String getRequiredPropertyOrThrowException(
            final Properties configProps, final String propKey) {
        final String value = configProps.getProperty(propKey);
        if (value == null) {
            final String errMsg = String.format(
                    "Required property %s was not defined.", propKey);
            throw new IllegalArgumentException(errMsg);
        }
        return value;
    }

    protected Properties getJavaMailProps(final Properties configProps) {
        final Properties mailProps = new Properties();
        final Set<Entry<Object, Object>> entrySet = configProps.entrySet();
        for (final Entry<Object, Object> entry : entrySet) {
            final String key = (String) entry.getKey();
            if (key.startsWith(JAVA_MAIL_PROP_PREFIX)) {
                final String value = (String) entry.getValue();
                mailProps.setProperty(key, value);
            }
        }
        return mailProps;
    }

    public boolean send() {

        final Session session = getMailSession(this.smtpUsername,
                this.smtpPassword);
        boolean sent = false;
        try {
            final MimeMessage message = new MimeMessage(session);
            final InternetAddress senderAdx = new InternetAddress(
                    this.smtpSender);
            message.setFrom(senderAdx);
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(this.smtpRecipients));
            message.setSubject(MESSAGE_SUBJECT);
            final StringBuilder messageText = new StringBuilder();
            for (final String report : this.reports) {
                messageText.append(report);
            }
            message.setContent(messageText.toString(), HTML_UTF8_MIME_TYPE);
            Transport.send(message);
            sent = true;
        } catch (final MessagingException e) {
            throw new RuntimeException(e);
        }
        return sent;
    }

    protected Session getMailSession(final String smtpUsername,
            final String smtpPassword) {
        final Session session = Session.getInstance(this.javaMailProps,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsername,
                                smtpPassword);
                    }
                });
        return session;
    }
}
