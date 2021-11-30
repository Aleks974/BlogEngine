package diplom.blogengine.service.util;

import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.exception.SendEmailFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.Locale;
import java.util.Objects;

@Slf4j
public class MailHelper {
    private final static String RESET_PASSWORD_CHANGE_PAGE = "/login/change-password/";

    private final BlogSettings blogSettings;
    private final MailSender emailSender;
    private final MessageSource messageSource;

    public MailHelper(BlogSettings blogSettings, MailSender emailSender, MessageSource messageSource) {
        this.emailSender = emailSender;
        this.blogSettings = blogSettings;
        this.messageSource = messageSource;
    }

    public void send(SimpleMailMessage mailMessage) {

    }

    public void sendResetPasswordEmail(String emailTo, String token, Locale locale) {
        log.debug("enter sendResetPasswordEmail()");

        Objects.requireNonNull(emailTo, "emailTo is null");
        Objects.requireNonNull(token, "token is null");
        Objects.requireNonNull(locale, "locale is null");

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(blogSettings.getEmail());
        mail.setTo(emailTo);
        String subject = String.format(messageSource.getMessage("message.resetPasswordSubject", null, locale), blogSettings.getTitle());
        mail.setSubject(subject);

        String link = blogSettings.getSiteUrl() + RESET_PASSWORD_CHANGE_PAGE + token;
        String message = String.format(messageSource.getMessage("message.resetPassword", null, locale), link);
        mail.setText(message);

        try {
            emailSender.send(mail);
        } catch (Exception ex) {
            throw new SendEmailFailedException("Error while sending Reset Password Email", ex);
        }
    }
}
