package pl.maciejkopec.cms.service;

import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.maciejkopec.cms.dto.Mail;
import reactor.core.publisher.Mono;

@Service
public class MailService {

  private final JavaMailSender mailSender;
  private final String mailTo;
  private final String subject;

  public MailService(
      final JavaMailSender mailSender,
      @Value("${application.mail.to}") final String mailTo,
      @Value("${application.mail.subject}") final String subject,
      final Validator validator) {
    this.mailSender = mailSender;
    this.mailTo = mailTo;
    this.subject = subject;
  }

  public Mono<Mail> send(final Mail mail) {
    try {
      final var message = new SimpleMailMessage();
      message.setFrom(mail.from());
      message.setReplyTo(mail.from());
      message.setTo(mailTo);
      message.setSubject(subject + mail.name());
      message.setText(mail.message());
      mailSender.send(message);
      return Mono.just(mail);
    } catch (final Exception ex) {
      return Mono.error(ex);
    }
  }
}
