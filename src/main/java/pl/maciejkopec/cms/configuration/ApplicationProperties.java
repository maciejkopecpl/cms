package pl.maciejkopec.cms.configuration;

import static java.util.Objects.requireNonNullElse;

import java.util.List;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("application")
@Getter
public class ApplicationProperties {

  private final Mail mail;
  private final String recaptchaSecret;
  private final List<String> allowedOrigins;

  public ApplicationProperties(
      final Mail mail, final String recaptchaSecret, final List<String> allowedOrigins) {
    this.mail = mail;
    this.recaptchaSecret = recaptchaSecret;
    this.allowedOrigins = requireNonNullElse(allowedOrigins, List.of());
  }

  @Getter
  public static class Mail {

    private final String to;
    private final String subject;

    public Mail(final String to, final String subject) {
      this.to = to;
      this.subject = subject;
    }
  }
}
