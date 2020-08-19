package pl.maciejkopec.cms.handler;

import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import pl.maciejkopec.cms.configuration.JacksonConfiguration;
import pl.maciejkopec.cms.configuration.SecurityConfiguration;
import pl.maciejkopec.cms.data.MailTestData;
import pl.maciejkopec.cms.dto.Mail;
import pl.maciejkopec.cms.router.MailerRouter;
import pl.maciejkopec.cms.service.MailService;
import pl.maciejkopec.cms.service.RecaptchaService;
import pl.maciejkopec.cms.service.ValidationService;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
      MailerRouter.class,
      MailerHandler.class,
      ValidationService.class,
      JacksonConfiguration.class,
      SecurityConfiguration.class
    })
@WebFluxTest
@AutoConfigureWebTestClient
public class MailerHandlerTest {

  @Autowired private WebTestClient webTestClient;
  @MockBean private MailService mailService;
  @MockBean private RecaptchaService recaptchaService;

  @Test
  public void shouldSendMail() {
    final var mail = MailTestData.valid();
    when(mailService.send(any(Mail.class))).thenReturn(Mono.just(mail));
    when(recaptchaService.validate(any(Mail.class))).thenReturn(Mono.just(mail));

    webTestClient
        .post()
        .uri("/mailer/")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(mail))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Mail.class)
        .isEqualTo(mail);

    verify(recaptchaService).validate(mail);
    verify(mailService).send(mail);
  }

  @ParameterizedTest
  @MethodSource("invalidMailProvider")
  public void shouldReturn404(final Mail invalidMail) {
    when(mailService.send(any(Mail.class))).thenReturn(Mono.just(invalidMail));
    when(recaptchaService.validate(any(Mail.class))).thenReturn(Mono.just(invalidMail));

    webTestClient
        .post()
        .uri("/mailer/")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(invalidMail))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody();
  }

  private static Stream<Arguments> invalidMailProvider() {
    return Stream.of(
        of(new Mail("", "Name", "Message", "token")),
        of(new Mail("from@mail.com", "", "Message", "token")),
        of(new Mail("from@mail.com", "Name", "", "token")),
        of(new Mail("from@mail.com", "Name", "Message", "")));
  }
}
