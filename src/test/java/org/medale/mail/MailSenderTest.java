package org.medale.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.medale.exsiter.ApplicationConfiguration;
import org.medale.exsiter.ApplicationConfigurationTest;

public class MailSenderTest {

    @Test
    public void testSendConfigureSunnyDayNotEnabled() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);
        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        configProps.remove(MailSender.PROP_SMTP_USERNAME);
        configProps.remove(MailSender.PROP_SMTP_PASSWORD);

        final MailSender sender = new MailSender();
        sender.configure(configProps);

        assertFalse(sender.isEnabled());
    }

    @Test
    public void testSendConfigureSunnyDay() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);

        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final MailSender sender = new MailSender();
        sender.configure(configProps);

        assertTrue(sender.isEnabled());

        final String[] actualConfigProps = { sender.getSmtpUsername(),
                sender.getSmtpPassword(), sender.getSmtpSender(),
                sender.getSmtpRecipients() };
        final String[] expectedConfigProps = {
                configProps.getProperty(MailSender.PROP_SMTP_USERNAME),
                configProps.getProperty(MailSender.PROP_SMTP_PASSWORD),
                configProps.getProperty(MailSender.PROP_SMTP_SENDER),
                configProps.getProperty(MailSender.PROP_SMTP_RECIPIENTS) };
        for (int i = 0; i < expectedConfigProps.length; i++) {
            final String expected = expectedConfigProps[i];
            final String actual = actualConfigProps[i];
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testSendConfigureMissingProperty() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);

        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        configProps.remove(MailSender.PROP_SMTP_PASSWORD);
        final MailSender sender = new MailSender();
        try {
            sender.configure(configProps);
        } catch (final Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            final String expectedErrMsg = "Required property smtp.password was not defined.";
            final String actualErrMsg = e.getMessage();
            assertEquals(expectedErrMsg, actualErrMsg);
        }
    }

    @Test
    public void testGetJavaMailProps() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);

        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        configProps.remove(MailSender.PROP_SMTP_PASSWORD);
        final MailSender sender = new MailSender();
        final Properties actualMailProps = sender.getJavaMailProps(configProps);
        final String[] expectedKeys = { "mail.smtp.starttls.enable",
                "mail.smtp.port", "mail.smtp.auth", "mail.smtp.host" };
        final String[] expectedValues = { "true", "587", "true",
                "smtp.gmail.com" };

        assertEquals(expectedKeys.length, actualMailProps.size());
        for (int i = 0; i < expectedValues.length; i++) {
            final String expectedKey = expectedKeys[i];
            final String expectedValue = expectedValues[i];
            assertTrue(actualMailProps.containsKey(expectedKey));
            final String actualValue = actualMailProps.getProperty(expectedKey);
            assertEquals(expectedValue, actualValue);
        }
    }

    @Test
    public void testSend() throws IOException {
        final ApplicationConfiguration appConfig = new ApplicationConfiguration(
                ApplicationConfigurationTest.TEST_CONFIG_LOCATION);

        appConfig.loadConfiguration();
        final Properties configProps = appConfig.getConfiguration();
        final MailSender sender = new MailSender();
        sender.configure(configProps);
        sender.addReport("<html><body><h1>Testing send...End report1</h1>\n");
        sender.addReport("Start report2: <h5>With two reports...</h5>End report2</body></html>");
        final boolean sent = sender.send();
        assertTrue(sent);
    }
}
