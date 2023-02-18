package pl.maciejkopec.cms.handler;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pl.maciejkopec.cms.dto.Mail;
import pl.maciejkopec.cms.service.MailService;
import pl.maciejkopec.cms.service.RecaptchaService;
import pl.maciejkopec.cms.service.ValidationService;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MailerHandler {

  private final MailService mailService;
  private final ValidationService validationService;
  private final RecaptchaService recaptchaService;

  @NotNull
  public Mono<ServerResponse> send(final ServerRequest request) {
    return request
        .bodyToMono(Mail.class)
        .flatMap(validationService::validate)
        .flatMap(recaptchaService::validate)
        .flatMap(mailService::send)
        .flatMap(mail -> ok().bodyValue(mail))
        .switchIfEmpty(ServerResponse.badRequest().build());
  }
}
