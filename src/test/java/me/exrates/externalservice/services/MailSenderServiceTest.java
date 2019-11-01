package me.exrates.externalservice.services;

import freemarker.template.Configuration;
import freemarker.template.Template;
import me.exrates.externalservice.model.enums.EmailType;
import me.exrates.externalservice.properties.ApplicationProperty;
import me.exrates.externalservice.properties.EmailProperty;
import me.exrates.externalservice.services.impl.MailSenderServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class MailSenderServiceTest extends AbstractTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private Configuration fmConfiguration;
    @Mock
    private EmailProperty emailProperty;
    @Mock
    private ApplicationProperty applicationProperty;

    private MailSenderService mailSenderService;

    @Before
    public void setUp() throws Exception {
        mailSenderService = spy(new MailSenderServiceImpl(mailSender, fmConfiguration, emailProperty, applicationProperty));
    }

    @Test
    public void send1_ok() throws Exception {
        doReturn("exrates.me")
                .when(applicationProperty)
                .getBaseUrl();
        doReturn("from@test.ru")
                .when(emailProperty)
                .getAddress();
        doReturn("Exrates")
                .when(emailProperty)
                .getName();
        doReturn("")
                .when(emailProperty)
                .getTitlePrefix();
        doReturn(new Template("name", "sourceCode", Configuration.getDefaultConfiguration()))
                .when(fmConfiguration)
                .getTemplate(anyString());
        doReturn(new MimeMessage(Session.getInstance(new Properties())))
                .when(mailSender)
                .createMimeMessage();
        doNothing()
                .when(mailSender)
                .send(any(MimeMessage.class));

        mailSenderService.send(EmailType.VERIFICATION, "Exrates", "to@test.ru", null);

        verify(applicationProperty, atLeastOnce()).getBaseUrl();
        verify(emailProperty, atLeastOnce()).getAddress();
        verify(emailProperty, atLeastOnce()).getName();
        verify(emailProperty, atLeastOnce()).getTitlePrefix();
        verify(fmConfiguration, atLeastOnce()).getTemplate(anyString());
        verify(mailSender, atLeastOnce()).createMimeMessage();
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    public void send2_ok() throws Exception {
        doReturn("exrates.me")
                .when(applicationProperty)
                .getBaseUrl();
        doReturn("from@test.ru")
                .when(emailProperty)
                .getAddress();
        doReturn("Exrates")
                .when(emailProperty)
                .getName();
        doReturn("")
                .when(emailProperty)
                .getTitlePrefix();
        doReturn(new Template("name", "sourceCode", Configuration.getDefaultConfiguration()))
                .when(fmConfiguration)
                .getTemplate(anyString());
        doReturn(new MimeMessage(Session.getInstance(new Properties())))
                .when(mailSender)
                .createMimeMessage();
        doNothing()
                .when(mailSender)
                .send(any(MimeMessage.class));

        mailSenderService.send(EmailType.VERIFICATION, "to@test.ru", null);

        verify(applicationProperty, atLeastOnce()).getBaseUrl();
        verify(emailProperty, atLeastOnce()).getAddress();
        verify(emailProperty, atLeastOnce()).getName();
        verify(emailProperty, atLeastOnce()).getTitlePrefix();
        verify(fmConfiguration, atLeastOnce()).getTemplate(anyString());
        verify(mailSender, atLeastOnce()).createMimeMessage();
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }
}