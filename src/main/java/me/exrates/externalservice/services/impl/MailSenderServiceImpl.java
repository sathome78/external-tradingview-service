package me.exrates.externalservice.services.impl;

import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import me.exrates.externalservice.entities.enums.EmailType;
import me.exrates.externalservice.properties.EmailProperty;
import me.exrates.externalservice.services.MailSenderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender mailSender;
    private final Configuration fmConfiguration;
    private final EmailProperty emailProperty;

    @Autowired
    public MailSenderServiceImpl(JavaMailSender mailSender,
                                 Configuration fmConfiguration,
                                 EmailProperty emailProperty) {
        this.mailSender = mailSender;
        this.fmConfiguration = fmConfiguration;
        this.emailProperty = emailProperty;
    }

    @Async
    @Override
    public void send(@NotNull EmailType type, @NotNull String to, Map<String, Object> properties) {
        send(type, type.getTitle(), to, properties);
    }

    @Async
    @Override
    public void send(@NotNull EmailType type, @NotNull String title, @NotNull String to, Map<String, Object> properties) {
        if (Objects.isNull(properties)) {
            properties = new HashMap<>();
        }

        String text = geFreeMarkerTemplateFromFile(type.getTemplate(), properties);

        if (Objects.nonNull(text)) {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(new InternetAddress(emailProperty.getAddress(), emailProperty.getName()));
                helper.setTo(to);
                helper.setSubject(StringUtils.join(emailProperty.getTitlePrefix(), title));
                helper.setText(text, true);
            } catch (Exception ex) {
                log.error("Messaging exception during setting MimeMessageHelper fields", ex);
            }
            mailSender.send(mimeMessage);
        }
    }

    private String geFreeMarkerTemplateFromFile(String template, Map<String, Object> model) {
        StringBuilder content = new StringBuilder();
        try {
            content.append(FreeMarkerTemplateUtils.processTemplateIntoString(
                    fmConfiguration.getTemplate(template),
                    model)
            );
            return content.toString();
        } catch (Exception ex) {
            log.warn("Exception occurred while processing fmTemplate: ", ex);
            return null;
        }
    }
}