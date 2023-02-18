package pl.maciejkopec.cms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pl.maciejkopec.cms.data.MailTestData.valid;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
public class RecaptchaServiceTest {

  private RecaptchaService recaptchaService;

  @Mock private WebClient.ResponseSpec responseSpecMock;

  @BeforeEach
  void setUp() {
    final var webClient = mock(WebClient.class);
    final var uriSpecMock = mock(WebClient.RequestBodyUriSpec.class);
    final var headersSpecMock = mock(WebClient.RequestBodySpec.class);

    when(webClient.post()).thenReturn(uriSpecMock);
    when(uriSpecMock.uri(
            ArgumentMatchers.notNull(),
            ArgumentMatchers.<String>notNull(),
            ArgumentMatchers.<String>notNull()))
        .thenReturn(headersSpecMock);
    when(headersSpecMock.accept(notNull())).thenReturn(headersSpecMock);
    when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);

    recaptchaService = new RecaptchaService(webClient, "");
  }

  @Test
  void shouldValidateToken() {
    when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<Object>>notNull()))
        .thenReturn(Mono.just(Map.of("success", true)));

    final var result = recaptchaService.validate(valid()).block();

    assertThat(result).isEqualTo(valid());
  }

  @Test
  void shouldVReturnErrorOnInvalidResponse() {
    when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<Object>>notNull()))
        .thenReturn(Mono.just(Map.of("success", false)));

    final var exception =
        Assertions.assertThrows(
            ResponseStatusException.class, () -> recaptchaService.validate(valid()).block());

    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(exception.getReason()).isEqualTo("Invalid ReCaptcha token.");
  }
}
