package pl.maciejkopec.cms.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import pl.maciejkopec.cms.dto.Mail;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RecaptchaService {

  private final WebClient webClient;
  private final String recaptchaSecret;

  public RecaptchaService(
      final WebClient webClient,
      @Value("${application.recaptcha-secret}") final String recaptchaSecret) {
    this.recaptchaSecret = recaptchaSecret;
    this.webClient = webClient;
  }

  public Mono<Mail> validate(final Mail mail) {
    return webClient
        .post()
        .uri(
            "https://www.google.com/recaptcha/api/siteverify?secret={secret}&response={response}",
            recaptchaSecret,
            mail.token())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(Map.class)
        .flatMap(
            map -> {
              if (map.get("success").equals(true)) {
                return Mono.just(mail);
              }
              return Mono.error(
                  new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ReCaptcha token."));
            });
  }
}
